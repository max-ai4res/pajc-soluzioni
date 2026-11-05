package it.unibs.pajc.redolfi.e04;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

/** Il protocollo del quiz si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void laRispostaTieneGliSpazi() {
        assertArrayEquals(new String[] {"la capitale e' Roma"}, Protocollo.argomenti(" la capitale e' Roma ", 1));
    }

    @Test
    void argomentiMancanti() {
        assertNull(Protocollo.argomenti("", 1));
        assertNull(Protocollo.argomenti("di troppo", 0));
        assertArrayEquals(new String[0], Protocollo.argomenti("  ", 0));
    }

    @Test
    void classificaAndataERitorno() {
        Map<String, Integer> punti = new TreeMap<>(Map.of("Marco", 3, "Giulia", 1));
        assertEquals("Giulia:1,Marco:3", Protocollo.classifica(punti));
        assertEquals(punti, Protocollo.classifica("Giulia:1,Marco:3"));
        assertTrue(Protocollo.classifica("").isEmpty());
        assertEquals(Map.of("Marco", 3), Protocollo.classifica("Marco:3,boh,Giulia:x"));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
