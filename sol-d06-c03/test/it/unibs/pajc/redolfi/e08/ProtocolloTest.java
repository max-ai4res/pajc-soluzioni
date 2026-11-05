package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La tabella con /storia e i due controlli: la forma (argomenti) e il valore di n. */
class ProtocolloTest {

    @Test
    void storiaEInTabella() {
        assertEquals(1, Protocollo.COMANDI.get("/storia").argomenti());
        assertArrayEquals(new String[] {"5"}, Protocollo.argomenti(" 5 ", 1));
    }

    @Test
    void nValido() {
        assertEquals(5, Protocollo.interoPositivo("5"));
        assertEquals(1, Protocollo.interoPositivo("1"));
    }

    @Test
    void nNonValido() {
        assertNull(Protocollo.interoPositivo("tre"));
        assertNull(Protocollo.interoPositivo("-2"));
        assertNull(Protocollo.interoPositivo("0"));
        assertNull(Protocollo.interoPositivo("2.5"));
    }

    @Test
    void privatoConSpazi() {
        assertArrayEquals(new String[] {"Giulia", "ci vediamo alle 10?"},
                Protocollo.argomenti("Giulia ci vediamo alle 10?", 2));
    }

    @Test
    void argomentiMancanti() {
        assertNull(Protocollo.argomenti("", 1));                  // /storia senza n
        assertNull(Protocollo.argomenti("Giulia", 2));            // /a senza testo
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
