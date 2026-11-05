package it.unibs.pajc.redolfi.e06;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Le sessioni del tris senza rete, con un Arbitro vero: due giocatori e uno spettatore
 * vivi insieme, una riga alla volta con elabora().
 */
class SessioneTest {

    private Arbitro arbitro;

    @BeforeEach
    void nuovoArbitro() {
        arbitro = new Arbitro();
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, arbitro).dialoga();
        return uscita.toString();
    }

    private Sessione giocatore(String nome, StringWriter uscita) {
        Sessione s = new Sessione(new StringReader(""), uscita, arbitro);
        s.elabora("/nome " + nome);
        return s;
    }

    @Test
    void primaIlNomePoiSiAspetta() throws IOException {
        assertTrue(dialogo("/m 0 0\n").contains("ERR prima scegli un nome"));
        assertTrue(dialogo("/nome Marco\n").contains("INFO in attesa di un avversario..."));
    }

    @Test
    void dueGiocanoEXComincia() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        giocatore("Marco", uMarco);
        giocatore("Giulia", uGiulia);
        assertTrue(uMarco.toString().contains("PARTITA X Giulia"));
        assertTrue(uMarco.toString().contains("TOCCA"));
        assertTrue(uGiulia.toString().contains("PARTITA O Marco"));
        assertFalse(uGiulia.toString().contains("TOCCA"));
    }

    @Test
    void leMosseSbagliate() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = giocatore("Marco", uMarco), giulia = giocatore("Giulia", uGiulia);
        giulia.elabora("/m 0 0");
        assertTrue(uGiulia.toString().contains("ERR non e' il tuo turno"));
        marco.elabora("/m 0 0");
        assertTrue(uMarco.toString().contains("MOSSA X 0 0"));
        assertTrue(uGiulia.toString().contains("MOSSA X 0 0"));
        assertTrue(uGiulia.toString().contains("TOCCA"));
        giulia.elabora("/m 0 0");
        assertTrue(uGiulia.toString().contains("ERR casella occupata"));
        giulia.elabora("/m 3 0");
        assertTrue(uGiulia.toString().contains("ERR riga e colonna vanno da 0 a 2"));
        giulia.elabora("/m 1");
        assertTrue(uGiulia.toString().contains("ERR uso: /m riga col"));
    }

    @Test
    void vittoriaDiX() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = giocatore("Marco", uMarco), giulia = giocatore("Giulia", uGiulia);
        marco.elabora("/m 0 0"); giulia.elabora("/m 1 0");
        marco.elabora("/m 0 1"); giulia.elabora("/m 1 1");
        marco.elabora("/m 0 2");
        assertTrue(uMarco.toString().contains("VINCE X"));
        assertTrue(uGiulia.toString().contains("VINCE X"));
    }

    @Test
    void loSpettatoreRiceveLoStatoEPoiLeMosse() {
        StringWriter uMarco = new StringWriter(), uSara = new StringWriter();
        Sessione marco = giocatore("Marco", uMarco);
        giocatore("Giulia", new StringWriter());
        Sessione sara = giocatore("Sara", uSara);
        sara.elabora("/guarda");
        assertTrue(uSara.toString().contains("PARTITE Marco-Giulia"));
        sara.elabora("/guarda Boh-Boh");
        assertTrue(uSara.toString().contains("ERR partita sconosciuta: Boh-Boh"));
        sara.elabora("/guarda Marco-Giulia");
        assertTrue(uSara.toString().contains("PARTITA X Marco-Giulia"));
        assertTrue(uSara.toString().contains("STATO ......... X"));
        marco.elabora("/m 1 1");
        assertTrue(uSara.toString().contains("MOSSA X 1 1"));
        marco.elabora("/guarda");
        assertTrue(uMarco.toString().contains("ERR stai giocando"));
    }

    @Test
    void esciSalutaEChiudeIlDialogo() throws IOException {
        String uscita = dialogo("/nome Marco\n/esci\n/guarda\n");
        assertTrue(uscita.trim().endsWith("CIAO"));
        assertFalse(uscita.contains("PARTITE"));
    }
}
