package it.unibs.pajc.redolfi.e05;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Il protocollo della chat con i file si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void treArgomentiPerInvia() {
        assertArrayEquals(new String[] {"Giulia", "foto.png", "1234"}, Protocollo.argomenti("Giulia foto.png 1234", 3));
        assertNull(Protocollo.argomenti("Giulia foto.png", 3));
    }

    @Test
    void ilRestoTieneGliSpazi() {
        assertArrayEquals(new String[] {"Giulia", "ci vediamo alle 10?"},
                Protocollo.argomenti("Giulia ci vediamo alle 10?", 2));
    }

    @Test
    void numero() {
        assertEquals(1234, Protocollo.numero(" 1234 "));
        assertEquals(-1, Protocollo.numero("tanti"));
        assertEquals(-1, Protocollo.numero("-5"));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
