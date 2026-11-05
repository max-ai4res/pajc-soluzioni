package it.unibs.pajc.redolfi.e07;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La tabella del quiz e il controllo degli argomenti, prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void laRispostaTieneGliSpazi() {
        assertArrayEquals(new String[] {"la capitale e' Roma"},
                Protocollo.argomenti("  la capitale e' Roma  ", 1));
    }

    @Test
    void argomentiMancanti() {
        assertNull(Protocollo.argomenti("", 1));                 // /r senza testo
        assertNull(Protocollo.argomenti("   ", 1));
        assertNull(Protocollo.argomenti("di troppo", 0));       // /classifica x
    }

    @Test
    void senzaArgomenti() {
        assertArrayEquals(new String[0], Protocollo.argomenti("  ", 0));
    }

    @Test
    void nomeValido() {
        assertTrue(Protocollo.nomeValido("Marco"));
        assertTrue(Protocollo.nomeValido("ab_1"));
        assertFalse(Protocollo.nomeValido("Marco Rossi"));
        assertFalse(Protocollo.nomeValido("M"));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }

    @Test
    void quantiArgomenti() {
        assertEquals(1, Protocollo.COMANDI.get("/nome").argomenti());
        assertEquals(1, Protocollo.COMANDI.get("/r").argomenti());
        assertEquals(0, Protocollo.COMANDI.get("/classifica").argomenti());
    }
}
