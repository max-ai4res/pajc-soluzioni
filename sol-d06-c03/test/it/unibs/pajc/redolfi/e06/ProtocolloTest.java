package it.unibs.pajc.redolfi.e06;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** La tabella e i due controlli, di forma e di valore, provati prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void dueOperandi() {
        assertArrayEquals(new String[] {"2", "3"}, Protocollo.argomenti("2 3", 2));
    }

    @Test
    void gliSpaziDoppiNonContano() {
        assertArrayEquals(new String[] {"2", "3"}, Protocollo.argomenti("  2   3 ", 2));
    }

    @Test
    void operandoMancante() {
        assertNull(Protocollo.argomenti("2", 2));
        assertNull(Protocollo.argomenti("2   ", 2));
        assertNull(Protocollo.argomenti("", 2));
    }

    @Test
    void senzaOperandi() {
        assertArrayEquals(new String[0], Protocollo.argomenti("   ", 0));
        assertNull(Protocollo.argomenti("di troppo", 0));
    }

    @Test
    void numeri() {
        assertArrayEquals(new long[] {2, -3}, Protocollo.numeri(new String[] {"2", "-3"}));
        assertArrayEquals(new long[0], Protocollo.numeri(new String[0]));
    }

    @Test
    void operandoNonNumerico() {
        assertNull(Protocollo.numeri(new String[] {"due", "3"}));
        assertNull(Protocollo.numeri(new String[] {"2", "3.5"}));
        assertNull(Protocollo.numeri(new String[] {"2", "3 4"}));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }

    @Test
    void leOperazioniVoglionoDueOperandi() {
        for (String op : List.of("ADD", "SUB", "MUL", "DIV"))
            assertEquals(2, Protocollo.COMANDI.get(op).operandi(), op);
        assertEquals(0, Protocollo.COMANDI.get("FINE").operandi());
    }
}
