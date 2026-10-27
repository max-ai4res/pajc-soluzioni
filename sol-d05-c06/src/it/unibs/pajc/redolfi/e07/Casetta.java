package it.unibs.pajc.redolfi.e07;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 6.7 - una casetta come una sola Area.
 *
 * La casetta e' disegnata in un quadrato di lato 100 "unita'" e poi scalata sul lato minore
 * del pannello: ogni coordinata e' x0 + u * lato / 100. L'area si costruisce in costruisci()
 * e si conserva; paintComponent la ricostruisce solo se la dimensione del pannello e'
 * cambiata dall'ultima volta. Corpo meno porta e finestre, piu' tetto, piu' comignolo.
 *
 * Errore tipico: ricostruire l'Area a ogni paintComponent (funziona, ma ripete calcoli
 * inutili), oppure sottrarre porta e finestre dopo aver unito il tetto, con lo stesso
 * risultato solo per caso.
 */
public class Casetta extends JPanel {

    private static final long serialVersionUID = 1L;

    private Area casetta;
    private int ultimaW = -1, ultimaH = -1;
    private double x0, y0, lato;

    public Casetta() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    private double x(double u) { return x0 + u * lato / 100; }
    private double y(double u) { return y0 + u * lato / 100; }
    private double l(double u) { return u * lato / 100; }

    private Rectangle2D rett(double ux, double uy, double uw, double uh) {
        return new Rectangle2D.Double(x(ux), y(uy), l(uw), l(uh));
    }

    private void costruisci() {
        lato = Math.min(getWidth(), getHeight()) * 0.9;
        x0 = (getWidth() - lato) / 2;
        y0 = (getHeight() - lato) / 2;

        Area corpo = new Area(rett(15, 45, 70, 50));
        corpo.subtract(new Area(rett(43, 68, 14, 27)));          // la porta
        corpo.subtract(new Area(rett(22, 55, 14, 12)));          // le finestre
        corpo.subtract(new Area(rett(64, 55, 14, 12)));

        Path2D tetto = new Path2D.Double();
        tetto.moveTo(x(8), y(47));
        tetto.lineTo(x(50), y(10));
        tetto.lineTo(x(92), y(47));
        tetto.closePath();
        corpo.add(new Area(tetto));
        corpo.add(new Area(rett(66, 14, 9, 22)));                 // il comignolo

        casetta = corpo;
        ultimaW = getWidth();
        ultimaH = getHeight();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getWidth() != ultimaW || getHeight() != ultimaH)
            costruisci();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, (float) y(10), new Color(0xC9, 0x4F, 0x2E),
                    0, (float) y(95), new Color(0x24, 0x54, 0x9C)));
            g2.fill(casetta);
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Casetta");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Casetta());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
