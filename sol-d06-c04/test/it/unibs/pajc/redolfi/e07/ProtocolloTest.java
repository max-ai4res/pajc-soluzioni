package it.unibs.pajc.redolfi.e07;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La tabella con le stanze si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void ilRestoTieneGliSpazi() {
        assertArrayEquals(new String[] {"Giulia", "ci vediamo alle 10?"},
                Protocollo.argomenti("Giulia ci vediamo alle 10?", 2));
    }

    @Test
    void argomentiMancanti() {
        assertNull(Protocollo.argomenti("Giulia", 2));           // manca il testo
        assertNull(Protocollo.argomenti("", 1));                 // /entra senza stanza
        assertNull(Protocollo.argomenti("di troppo", 0));        // /stanze con un argomento
    }

    @Test
    void laTabellaHaLeStanze() {
        assertTrue(Protocollo.COMANDI.containsKey("/entra"));
        assertTrue(Protocollo.COMANDI.containsKey("/stanze"));
        assertTrue(Protocollo.COMANDI.get("/entra").argomenti() == 1);
        assertTrue(Protocollo.COMANDI.get("/stanze").argomenti() == 0);
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }

    @Test
    void ilNomeDiStanza() {
        assertTrue(Protocollo.stanzaValida("giochi"));
        assertTrue(Protocollo.stanzaValida("sala-2"));
        assertFalse(Protocollo.stanzaValida("Sala Grande"));     // spazi e maiuscole
        assertFalse(Protocollo.stanzaValida("a:b"));             // i separatori di STANZE
        assertFalse(Protocollo.stanzaValida("x"));
    }
}
