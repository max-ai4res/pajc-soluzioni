package it.unibs.pajc.redolfi.e09;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La tabella del tris si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void laMossaHaDueArgomenti() {
        assertEquals(2, Protocollo.COMANDI.get("/m").argomenti());
        assertArrayEquals(new String[] {"1", "2"}, Protocollo.argomenti("1 2", 2));
        assertArrayEquals(new String[] {"1", "2 3"}, Protocollo.argomenti("1 2 3", 2));   // poi coordinata dice no
        assertNull(Protocollo.argomenti("1", 2));
        assertNull(Protocollo.argomenti("", 2));
    }

    @Test
    void leCoordinateVannoDaZeroADue() {
        assertEquals(0, Protocollo.coordinata("0"));
        assertEquals(2, Protocollo.coordinata("2"));
        assertEquals(-1, Protocollo.coordinata("3"));
        assertEquals(-1, Protocollo.coordinata("a"));
        assertEquals(-1, Protocollo.coordinata("2 3"));
        assertEquals(-1, Protocollo.coordinata("10"));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
