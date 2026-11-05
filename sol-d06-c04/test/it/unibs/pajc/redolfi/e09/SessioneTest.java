package it.unibs.pajc.redolfi.e09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Una partita senza rete: due sessioni su stringhe, un Arbitro vero. Con elabora() si
 * manda una riga alla volta; con run() su un input che finisce si prova anche il
 * finally, cioe' l'abbandono.
 */
class SessioneTest {

    private Arbitro arbitro;
    private StringWriter uMarco, uGiulia;
    private Sessione marco, giulia;

    @BeforeEach
    void dueGiocatori() {
        arbitro = new Arbitro();
        uMarco = new StringWriter();
        uGiulia = new StringWriter();
        marco = new Sessione(new StringReader(""), uMarco, arbitro);
        giulia = new Sessione(new StringReader(""), uGiulia, arbitro);
    }

    private void accoppiati() {
        marco.elabora("/nome Marco");
        giulia.elabora("/nome Giulia");
    }

    @Test
    void ilSecondoCheEntraAvviaLaPartita() {
        marco.elabora("/nome Marco");
        assertTrue(uMarco.toString().contains("INFO in attesa"));
        giulia.elabora("/nome Giulia");
        assertTrue(uMarco.toString().contains("PARTITA X Giulia"));
        assertTrue(uGiulia.toString().contains("PARTITA O Marco"));
        assertTrue(uMarco.toString().contains("TOCCA"));
        assertFalse(uGiulia.toString().contains("TOCCA"));
    }

    @Test
    void ilTurnoELaCasella() {
        accoppiati();
        giulia.elabora("/m 0 0");
        assertTrue(uGiulia.toString().contains("ERR non e' il tuo turno"));
        marco.elabora("/m 0 0");
        assertTrue(uMarco.toString().contains("MOSSA X 0 0"));
        assertTrue(uGiulia.toString().contains("MOSSA X 0 0"));
        assertTrue(uGiulia.toString().contains("TOCCA"));
        giulia.elabora("/m 0 0");
        assertTrue(uGiulia.toString().contains("ERR casella occupata"));
    }

    @Test
    void mosseNonValide() {
        accoppiati();
        marco.elabora("/m 3 0");
        assertTrue(uMarco.toString().contains("ERR riga e colonna vanno da 0 a 2"));
        marco.elabora("/m 1 2 3");
        assertEquals(2, uMarco.toString().split("ERR riga e colonna", -1).length - 1);
        marco.elabora("/m 1");
        assertTrue(uMarco.toString().contains("ERR uso: /m R C"));
        marco.elabora("ciao");
        assertTrue(uMarco.toString().contains("ERR comando sconosciuto: ciao"));
        assertFalse(uMarco.toString().contains("MOSSA"));
    }

    @Test
    void senzaPartitaNonSiMuove() {
        marco.elabora("/m 0 0");
        assertTrue(uMarco.toString().contains("ERR prima scegli un nome"));
        marco.elabora("/nome Marco");
        marco.elabora("/m 0 0");
        assertTrue(uMarco.toString().contains("ERR non sei in una partita"));
    }

    @Test
    void treInFilaVinconoEPoiSiTornaInCoda() {
        accoppiati();
        marco.elabora("/m 0 0");
        giulia.elabora("/m 1 0");
        marco.elabora("/m 0 1");
        giulia.elabora("/m 1 1");
        marco.elabora("/m 0 2");
        assertTrue(uMarco.toString().contains("VINCE X"));
        assertTrue(uGiulia.toString().contains("VINCE X"));
        String dopo = uMarco.toString().substring(uMarco.toString().indexOf("VINCE X"));
        assertTrue(dopo.contains("PARTITA"));                 // riaccoppiati subito
        giulia.elabora("/m 2 2");
        assertFalse(uGiulia.toString().contains("ERR la partita e' finita"));   // e' una partita nuova
    }

    @Test
    void grigliaPienaSenzaTreInFila() {
        accoppiati();
        String[] mosse = {"0 0", "1 1", "2 2", "0 1", "2 1", "2 0", "0 2", "1 2", "1 0"};
        for (int i = 0; i < mosse.length; i++)
            (i % 2 == 0 ? marco : giulia).elabora("/m " + mosse[i]);
        assertTrue(uMarco.toString().contains("PATTA"));
        assertTrue(uGiulia.toString().contains("PATTA"));
        assertFalse(uMarco.toString().contains("VINCE"));
    }

    @Test
    void chiSeNeVaAMetaPerde() {
        giulia.elabora("/nome Giulia");                       // prima in coda: sara' X
        Sessione marcoCheStacca = new Sessione(new StringReader("/nome Marco\n"), uMarco, arbitro);
        marcoCheStacca.run();                                 // l'input finisce: come un cavo staccato
        assertTrue(uGiulia.toString().contains("PARTITA X Marco"));
        assertTrue(uMarco.toString().contains("PARTITA O Giulia"));
        assertTrue(uGiulia.toString().contains("ABBANDONO Marco"));
        String dopo = uGiulia.toString().substring(uGiulia.toString().indexOf("ABBANDONO"));
        assertTrue(dopo.contains("INFO in attesa"));          // Giulia e' di nuovo libera
    }

    @Test
    void esciSalutaEChiude() {
        Sessione solo = new Sessione(new StringReader("/nome Luca\n/esci\n/m 0 0\n"), uMarco, arbitro);
        solo.run();
        assertTrue(uMarco.toString().trim().endsWith("CIAO"));
    }
}
