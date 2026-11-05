package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

/** Il protocollo di WhatsPAJC completa si prova prima di aprire un socket. */
class ProtocolloTest {

    @Test
    void ilRestoTieneGliSpazi() {
        assertArrayEquals(new String[] {"Giulia", "ci vediamo alle 10?"},
                Protocollo.argomenti("Giulia ci vediamo alle 10?", 2));
    }

    @Test
    void argomentiMancanti() {
        assertNull(Protocollo.argomenti("Giulia", 2));
        assertNull(Protocollo.argomenti("", 1));
        assertNull(Protocollo.argomenti("di troppo", 0));
        assertArrayEquals(new String[0], Protocollo.argomenti("   ", 0));
    }

    @Test
    void nomiEStanze() {
        assertTrue(Protocollo.nomeValido("Marco_1"));
        assertFalse(Protocollo.nomeValido("M"));
        assertFalse(Protocollo.nomeValido("Marco Rossi"));
        assertTrue(Protocollo.stanzaValida("pajc-2027"));
        assertFalse(Protocollo.stanzaValida("Pajc"));           // solo minuscole: finisce in STANZE
        assertFalse(Protocollo.stanzaValida("a:b"));
    }

    @Test
    void stanzeAndataERitorno() {
        Map<String, Integer> stanze = new TreeMap<>(Map.of("generale", 3, "pajc", 1));
        assertEquals("generale:3,pajc:1", Protocollo.stanze(stanze));
        assertEquals(stanze, Protocollo.stanze("generale:3,pajc:1"));
        assertTrue(Protocollo.stanze("").isEmpty());
        assertEquals(Map.of("pajc", 1), Protocollo.stanze("generale:x,pajc:1"));
    }

    @Test
    void ogniAiutoIniziaConIlSuoComando() {
        Protocollo.COMANDI.forEach((comando, a) -> assertTrue(a.aiuto().startsWith(comando), comando));
    }
}
