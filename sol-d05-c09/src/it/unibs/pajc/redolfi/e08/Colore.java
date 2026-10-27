package it.unibs.pajc.redolfi.e08;

import java.awt.Color;

/** Un colore con il suo nome: un valore, quindi un record (capitolo 3). */
public record Colore(String nome, Color colore) {

    public static final Colore[] PALETTE = {
            new Colore("Nero", Color.BLACK),
            new Colore("Blu", new Color(0x24, 0x54, 0x9C)),
            new Colore("Rosso", new Color(0xC9, 0x4F, 0x2E)),
            new Colore("Verde", new Color(0x2E, 0x7D, 0x4C)),
            new Colore("Arancio", new Color(0xE0, 0x8A, 0x1E)) };

    @Override
    public String toString() {
        return nome;                    // quello che la tendina mostra
    }
}
