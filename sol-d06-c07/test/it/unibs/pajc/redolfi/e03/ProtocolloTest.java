package it.unibs.pajc.redolfi.e03;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Il protocollo della calcolatrice si prova prima di aprire un socket: forma e valore. */
class ProtocolloTest {

    @Test
    void dueOperandi() {
        assertArrayEquals(new String[] {"2", "3"}, Protocollo.argomenti("2 3", 2));
        assertArrayEquals(new String[] {"2", "3"}, Protocollo.argomenti("   2   3 ", 2));
    }

    @Test
    void operandoMancante() {
        assertNull(Protocollo.argomenti("2", 2));
        assertNull(Protocollo.argomenti("", 2));
    }

    @Test
    void senzaOperandi() {
        assertArrayEquals(new String[0], Protocollo.argomenti("  ", 0));
        assertNull(Protocollo.argomenti("di troppo", 0));
    }

    @Test
    void numeri() {
        assertArrayEquals(new long[] {2, -3}, Protocollo.numeri(new String[] {"2", "-3"}));
        assertNull(Protocollo.numeri(new String[] {"due", "3"}));
        assertNull(Protocollo.numeri(new String[] {"2", "3.5"}));
    }

    @Test
    void ogniAiutoIniziaConLaSuaOperazione() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
