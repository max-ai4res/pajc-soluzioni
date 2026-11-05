package it.unibs.pajc.redolfi.e05;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * La Sessione senza rete, con un Registro vero e un Deposito vero (su una porta a caso,
 * mai avviato: qui servono solo le offerte). Le righe di testo dei file si provano cosi';
 * i byte sul secondo socket no.
 */
class SessioneTest {

    private Registro registro;
    private Deposito deposito;

    @BeforeEach
    void nuovoStato() throws IOException {
        registro = new Registro();
        deposito = new Deposito(0);
    }

    @AfterEach
    void chiudi() throws IOException {
        deposito.chiudi();
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, registro, deposito).dialoga();
        return uscita.toString();
    }

    private Sessione utente(String nome, StringWriter uscita) {
        Sessione s = new Sessione(new StringReader(""), uscita, registro, deposito);
        s.elabora("/nome " + nome);
        return s;
    }

    @Test
    void primaIlNome() throws IOException {
        assertTrue(dialogo("ciao a tutti\n").contains("ERR prima scegli un nome"));
        assertTrue(dialogo("/aiuto\n").contains("INFO /invia N f d"));
    }

    @Test
    void usoSbagliato() throws IOException {
        assertTrue(dialogo("/nome Marco\n/invia Giulia foto.png\n").contains("ERR uso: /invia N f d"));
        assertTrue(dialogo("/nome Marco\n/accetta subito\n").contains("ERR uso: /accetta"));
    }

    @Test
    void offertaEAccettazione() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = utente("Marco", uMarco), giulia = utente("Giulia", uGiulia);
        marco.elabora("/invia Giulia foto.png 1234");
        assertTrue(uMarco.toString().contains("CARICA 1"));
        assertTrue(uGiulia.toString().contains("FILE Marco foto.png 1234 1"));
        giulia.elabora("/accetta");
        assertTrue(uGiulia.toString().contains("PRENDI 1 foto.png"));
        giulia.elabora("/accetta");
        assertTrue(uGiulia.toString().contains("ERR nessun file da accettare"));
    }

    @Test
    void offertaChenonVa() {
        StringWriter uMarco = new StringWriter();
        Sessione marco = utente("Marco", uMarco);
        marco.elabora("/invia Luca foto.png 1234");
        assertTrue(uMarco.toString().contains("ERR utente sconosciuto: Luca"));
        marco.elabora("/invia Marco foto.png tanti");
        assertTrue(uMarco.toString().contains("ERR dimensione non valida: tanti"));
        assertFalse(uMarco.toString().contains("CARICA"));
    }

    @Test
    void unMessaggioArrivaATuttiMittenteCompreso() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = utente("Marco", uMarco);
        Sessione giulia = utente("Giulia", uGiulia);
        marco.elabora("buongiorno a tutti");
        giulia.elabora("/a Marco ciao Marco");
        assertTrue(uMarco.toString().contains("ENTRA Giulia"));
        assertTrue(uMarco.toString().contains("MSG Marco buongiorno a tutti"));
        assertTrue(uGiulia.toString().contains("MSG Marco buongiorno a tutti"));
        assertTrue(uMarco.toString().contains("PRIV Giulia Marco ciao Marco"));
        assertTrue(uGiulia.toString().contains("PRIV Giulia Marco ciao Marco"));
    }

    @Test
    void esciSalutaEChiudeIlDialogo() throws IOException {
        String uscita = dialogo("/nome Marco\n/esci\nquesto non arriva\n");
        assertTrue(uscita.trim().endsWith("CIAO"));
        assertFalse(uscita.contains("MSG"));
    }
}
