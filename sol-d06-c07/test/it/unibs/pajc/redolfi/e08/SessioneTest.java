package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * La Sessione senza rete: legge da una stringa, scrive in una stringa, il Registro e'
 * vero. Con dialoga() il dialogo va fino alla fine dell'input; con elabora() si manda
 * una riga alla volta a piu' sessioni vive insieme, anche in stanze diverse.
 */
class SessioneTest {

    private Registro registro;

    @BeforeEach
    void nuovoRegistro() {
        registro = new Registro();
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, registro).dialoga();
        return uscita.toString();
    }

    private Sessione utente(String nome, StringWriter uscita) {
        Sessione s = new Sessione(new StringReader(""), uscita, registro);
        s.elabora("/nome " + nome);
        return s;
    }

    @Test
    void primaIlNomeMaPingEAiutoValgonoSubito() throws IOException {
        String uscita = dialogo("ciao a tutti\n/ping\n/aiuto\n");
        assertTrue(uscita.contains("ERR prima scegli un nome"));
        assertTrue(uscita.contains("PONG"));
        assertTrue(uscita.contains("INFO /entra S"));
    }

    @Test
    void usoSbagliatoEComandoSconosciuto() throws IOException {
        String uscita = dialogo("/nome Marco\n/a Giulia\n/entra\n/boh x\n/chi\n");
        assertTrue(uscita.contains("ERR uso: /a N testo"));
        assertTrue(uscita.contains("ERR uso: /entra S"));
        assertTrue(uscita.contains("ERR comando sconosciuto: /boh"));
        assertTrue(uscita.contains("UTENTI Marco"));
    }

    @Test
    void dueConLoStessoNome() {
        StringWriter uMarco = new StringWriter(), uAltro = new StringWriter();
        utente("Marco", uMarco);
        utente("Marco", uAltro);
        assertTrue(uMarco.toString().contains("BENVENUTO Marco"));
        assertTrue(uAltro.toString().contains("ERR nome gia' in uso: Marco"));
    }

    @Test
    void unMessaggioArrivaAllaStanzaMittenteCompreso() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = utente("Marco", uMarco);
        Sessione giulia = utente("Giulia", uGiulia);
        marco.elabora("buongiorno a tutti");
        giulia.elabora("/a Marco ciao Marco");
        giulia.elabora("/a Luca ciao Luca");
        assertTrue(uMarco.toString().contains("ENTRA Giulia"));
        assertTrue(uMarco.toString().contains("MSG Marco buongiorno a tutti"));
        assertTrue(uGiulia.toString().contains("MSG Marco buongiorno a tutti"));
        assertTrue(uMarco.toString().contains("PRIV Giulia Marco ciao Marco"));
        assertTrue(uGiulia.toString().contains("PRIV Giulia Marco ciao Marco"));
        assertTrue(uGiulia.toString().contains("ERR utente sconosciuto: Luca"));
    }

    @Test
    void leStanzeSeparanoIMessaggiMaNonIPrivati() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = utente("Marco", uMarco);
        Sessione giulia = utente("Giulia", uGiulia);
        giulia.elabora("/entra pajc");
        assertTrue(uGiulia.toString().contains("STANZA pajc"));
        assertTrue(uMarco.toString().contains("ESCE Giulia"));
        marco.elabora("solo per generale");
        assertFalse(uGiulia.toString().contains("MSG Marco solo per generale"));
        marco.elabora("/chi");
        assertTrue(uMarco.toString().contains("UTENTI Marco\n"));
        giulia.elabora("/stanze");
        assertTrue(uGiulia.toString().contains("STANZE generale:1,pajc:1"));
        marco.elabora("/a Giulia psst");
        assertTrue(uGiulia.toString().contains("PRIV Marco Giulia psst"));
        giulia.elabora("/entra pajc");
        assertTrue(uGiulia.toString().contains("ERR sei gia' in pajc"));
        giulia.elabora("/entra Pajc!");
        assertTrue(uGiulia.toString().contains("ERR nome di stanza non valido: Pajc!"));
    }

    @Test
    void esciSalutaEChiudeIlDialogo() throws IOException {
        String uscita = dialogo("/nome Marco\n/esci\nquesto non arriva\n");
        assertTrue(uscita.trim().endsWith("CIAO"));
        assertFalse(uscita.contains("MSG"));
    }
}
