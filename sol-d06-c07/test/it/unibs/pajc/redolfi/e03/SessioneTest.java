package it.unibs.pajc.redolfi.e03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

/** La Sessione senza rete: una riga per ogni caso della tabella e per ogni modo di sbagliarla. */
class SessioneTest {

    private List<String> dialogo(String righe, Statistiche statistiche) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, statistiche).dialoga();
        List<String> lines = uscita.toString().lines().toList();
        assertTrue(lines.get(0).startsWith("INFO "));                 // il saluto
        return lines.subList(1, lines.size());                        // poi una riga per richiesta
    }

    @Test
    void unaRispostaPerOgniRiga() throws IOException {
        List<String> r = dialogo("ADD 2 3\nsub 10 4\nMUL 6 7\nDIV 7 2\nDIV 1 0\nADD due 3\nADD 2\nPOW 2 3\n\n", new Statistiche());
        assertEquals(List.of("5", "6", "42", "3",
                "ERR divisione per zero",
                "ERR operando non numerico: due 3",
                "ERR uso: ADD a b   la somma",
                "ERR operazione sconosciuta: POW",
                "ERR riga vuota"), r);
    }

    @Test
    void fineChiudeIlDialogo() throws IOException {
        assertEquals(List.of("5", "CIAO"), dialogo("ADD 2 3\nfine\nADD 1 1\n", new Statistiche()));
    }

    @Test
    void statContaLeOperazioniDiTutti() throws IOException {
        Statistiche s = new Statistiche();
        dialogo("ADD 2 3\nMUL 2 2\nDIV 1 0\n", s);                     // l'ERR non conta
        assertEquals(List.of("STAT 2 0"), dialogo("STAT\n", s));       // 0 collegati: nessun run()
    }

    @Test
    void aiutoEscedallaTabella() throws IOException {
        List<String> r = dialogo("AIUTO\n", new Statistiche());
        assertEquals(Protocollo.COMANDI.size(), r.size());
        assertTrue(r.get(0).startsWith("INFO ADD a b"));
    }
}
