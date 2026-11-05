package it.unibs.pajc.redolfi.e06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * La Sessione senza rete: legge da una stringa, scrive in una stringa. Un test per ogni
 * riga della tabella e uno per ogni modo di sbagliarla.
 */
class SessioneTest {

    /** Le righe di risposta, senza l'INFO di benvenuto. */
    private List<String> risposte(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita).dialoga();
        List<String> tutte = uscita.toString().lines().toList();
        assertTrue(tutte.get(0).startsWith("INFO "));
        return tutte.subList(1, tutte.size());
    }

    @Test void add() throws IOException { assertEquals(List.of("5"), risposte("ADD 2 3\n")); }
    @Test void sub() throws IOException { assertEquals(List.of("6"), risposte("SUB 10 4\n")); }
    @Test void mul() throws IOException { assertEquals(List.of("42"), risposte("MUL 6 7\n")); }
    @Test void div() throws IOException { assertEquals(List.of("-3"), risposte("DIV -7 2\n")); }

    @Test
    void minuscoleESpaziInPiu() throws IOException {
        assertEquals(List.of("5"), risposte("  add   2  3 \n"));
    }

    @Test
    void divisionePerZero() throws IOException {
        assertEquals(List.of("ERR divisione per zero"), risposte("DIV 1 0\n"));
    }

    @Test
    void operandoMancante() throws IOException {
        assertEquals(List.of("ERR uso: ADD a b   la somma"), risposte("ADD 2\n"));
        assertEquals(List.of("ERR uso: MUL a b   il prodotto"), risposte("MUL\n"));
    }

    @Test
    void operandoNonNumerico() throws IOException {
        assertEquals(List.of("ERR operando non numerico: due 3"), risposte("ADD due 3\n"));
    }

    @Test
    void operazioneSconosciuta() throws IOException {
        assertEquals(List.of("ERR operazione sconosciuta: POW"), risposte("POW 2 3\n"));
    }

    @Test
    void unaRigaSbagliataNonChiude() throws IOException {
        assertEquals(List.of("ERR operazione sconosciuta: boh", "5"), risposte("boh\nADD 2 3\n"));
    }

    @Test
    void laRigaVuotaNonRisponde() throws IOException {
        assertEquals(List.of("5"), risposte("\n   \nADD 2 3\n"));
    }

    @Test
    void fineSalutaEChiudeIlDialogo() throws IOException {
        assertEquals(List.of("CIAO"), risposte("fine\nADD 2 3\n"));
    }

    @Test
    void aiutoElencaLaTabella() throws IOException {
        List<String> righe = risposte("AIUTO\n");
        assertEquals(Protocollo.COMANDI.size(), righe.size());
        for (String comando : Protocollo.COMANDI.keySet())
            assertTrue(righe.stream().anyMatch(r -> r.startsWith("INFO " + comando)), comando);
    }
}
