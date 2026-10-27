package it.unibs.pajc.redolfi.e06;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 5.6 - una griglia 10x10 che si adatta al pannello.
 *
 * Il lato della casella e' il piu' grande che fa stare 10 caselle nel lato MINORE del
 * pannello; la griglia si centra con il margine che avanza. Le misure si ricalcolano a ogni
 * paintComponent. Le linee sono 11 per direzione (da 0 a 10 caselle comprese); il bordo
 * esterno si disegna per ultimo, sopra le linee grigie.
 *
 * Errore tipico: un ciclo da 0 a 9 per le linee, che dimentica il bordo destro e quello
 * inferiore; oppure il lato calcolato solo sulla larghezza.
 */
public class Griglia extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int N = 10;

    public Griglia() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int c = Math.min(getWidth(), getHeight()) / N;
        int x0 = (getWidth() - c * N) / 2, y0 = (getHeight() - c * N) / 2;

        g.setColor(new Color(0xB8, 0xD8, 0xF0));
        for (int i = 0; i < N; i++)
            g.fillRect(x0 + i * c, y0 + i * c, c, c);          // la diagonale

        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= N; i++) {
            g.drawLine(x0 + i * c, y0, x0 + i * c, y0 + N * c);  // verticali
            g.drawLine(x0, y0 + i * c, x0 + N * c, y0 + i * c);  // orizzontali
        }
        g.setColor(Color.BLACK);
        g.drawRect(x0, y0, N * c, N * c);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Griglia");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Griglia());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
