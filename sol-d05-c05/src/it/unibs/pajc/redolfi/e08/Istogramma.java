package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 5.8 - un istogramma con le etichette centrate.
 *
 * Lo spazio verticale si divide in tre fasce: una riga di testo in alto per i valori, le
 * barre, una riga in basso per le etichette; l'altezza di una riga e' fm.getHeight(). La
 * barra piu' alta occupa tutta la fascia centrale, le altre in proporzione. Ogni testo si
 * centra sulla barra con stringWidth. I colori si distribuiscono sulla ruota delle tinte.
 *
 * Errore tipico: divisione intera valore / massimo, che da' 0 per tutte le barre tranne la
 * piu' alta; si moltiplica prima e si divide dopo.
 */
public class Istogramma extends JPanel {

    private static final long serialVersionUID = 1L;

    private final int[] valori;
    private final String[] etichette;

    public Istogramma(int[] valori, String[] etichette) {
        this.valori = valori.clone();
        this.etichette = etichette.clone();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(560, 320));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FontMetrics fm = g.getFontMetrics();
        int riga = fm.getHeight(), margine = 10;
        int n = valori.length;
        int max = 1;
        for (int v : valori)
            max = Math.max(max, v);

        int larghezza = (getWidth() - 2 * margine) / n;
        int barra = larghezza * 3 / 4;
        int fondo = getHeight() - margine - riga;              // la base delle barre
        int altezzaMax = fondo - margine - riga;

        for (int i = 0; i < n; i++) {
            int h = valori[i] * altezzaMax / max;               // prima la moltiplicazione
            int cx = margine + i * larghezza + larghezza / 2;
            g.setColor(Color.getHSBColor(i / (float) n, 0.6f, 0.85f));
            g.fillRect(cx - barra / 2, fondo - h, barra, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(cx - barra / 2, fondo - h, barra, h);
            centrato(g, fm, Integer.toString(valori[i]), cx, fondo - h - fm.getDescent() - 2);
            centrato(g, fm, etichette[i], cx, fondo + fm.getAscent() + 2);
        }
    }

    private static void centrato(Graphics g, FontMetrics fm, String s, int cx, int base) {
        g.drawString(s, cx - fm.stringWidth(s) / 2, base);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Istogramma");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Istogramma(new int[] { 12, 30, 7, 22, 41, 18 },
                    new String[] { "lun", "mar", "mer", "gio", "ven", "sab" }));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
