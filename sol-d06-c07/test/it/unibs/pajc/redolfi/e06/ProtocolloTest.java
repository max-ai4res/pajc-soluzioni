package it.unibs.pajc.redolfi.e06;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Il protocollo del tris si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void rigaEColonna() {
        assertArrayEquals(new String[] {"1", "2"}, Protocollo.argomenti("1 2", 2));
        assertNull(Protocollo.argomenti("1", 2));
    }

    @Test
    void coordinata() {
        assertEquals(0, Protocollo.coordinata("0"));
        assertEquals(2, Protocollo.coordinata(" 2 "));
        assertEquals(-1, Protocollo.coordinata("3"));
        assertEquals(-1, Protocollo.coordinata("uno"));
        assertEquals(-1, Protocollo.coordinata("1 2"));
    }

    @Test
    void guardaHaLArgomentoFacoltativo() {
        assertTrue(Protocollo.COMANDI.get("/guarda").facoltativi());
        assertFalse(Protocollo.COMANDI.get("/m").facoltativi());
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
