package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La tabella con /ping si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void pingNonVuoleArgomenti() {
        assertEquals(0, Protocollo.COMANDI.get("/ping").argomenti());
        assertArrayEquals(new String[0], Protocollo.argomenti("  ", 0));
        assertNull(Protocollo.argomenti("di troppo", 0));
    }

    @Test
    void ilRestoTieneGliSpazi() {
        assertArrayEquals(new String[] {"Giulia", "ci vediamo alle 10?"},
                Protocollo.argomenti("Giulia ci vediamo alle 10?", 2));
        assertNull(Protocollo.argomenti("Giulia", 2));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
