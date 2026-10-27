package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 7.8 - un sistema solare fermo, con trasformazioni annidate.
 *
 * Le distanze sono in "unita' del sistema" (UA, unita' astronomiche approssimate): il
 * pannello si porta l'origine al centro e scala perche' l'orbita piu' esterna stia nel lato
 * minore. Ogni pianeta si disegna su una copia del contesto del Sole: rotate per l'angolo,
 * translate sulla distanza. La luna nasce da una copia del contesto del SUO pianeta, gia'
 * ruotato e traslato: una seconda rotazione e traslazione la portano sulla sua orbita.
 *
 * Errore tipico: disegnare la luna dal contesto del Sole, che la mette in orbita attorno al
 * centro del pannello.
 */
public class SistemaSolare extends JPanel {

    private static final long serialVersionUID = 1L;

    record Pianeta(double distanza, double raggio, double angolo, Color colore) { }

    static final Pianeta[] PIANETI = {
            new Pianeta(1.0, 0.10, Math.toRadians(30), new Color(0xB0, 0x8D, 0x57)),
            new Pianeta(1.8, 0.16, Math.toRadians(160), new Color(0xE0, 0x8A, 0x1E)),
            new Pianeta(3.0, 0.20, Math.toRadians(-60), new Color(0x24, 0x54, 0x9C)) };

    public SistemaSolare() {
        setBackground(new Color(0x10, 0x18, 0x20));
        setPreferredSize(new Dimension(500, 500));
    }

    static Ellipse2D cerchio(double r) {
        return new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scala = Math.min(getWidth(), getHeight()) / 2.0 / 3.5;   // pixel per unita'
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(scala, scala);

            g2.setColor(new Color(0xFF, 0xD2, 0x4A));
            g2.fill(cerchio(0.35));                                  // il Sole
            for (int i = 0; i < PIANETI.length; i++) {
                Pianeta p = PIANETI[i];
                g2.setColor(new Color(0x60, 0x6C, 0x7C));
                g2.setStroke(new java.awt.BasicStroke((float) (1 / scala)));   // un pixel
                g2.draw(cerchio(p.distanza()));                      // l'orbita

                Graphics2D gp = (Graphics2D) g2.create();
                gp.rotate(p.angolo());
                gp.translate(p.distanza(), 0);
                gp.setColor(p.colore());
                gp.fill(cerchio(p.raggio()));
                if (i == PIANETI.length - 1) {                       // la luna, dal pianeta
                    gp.setColor(new Color(0x60, 0x6C, 0x7C));
                    gp.draw(cerchio(0.45));
                    gp.rotate(Math.toRadians(70));
                    gp.translate(0.45, 0);
                    gp.setColor(Color.LIGHT_GRAY);
                    gp.fill(cerchio(0.07));
                }
                gp.dispose();
            }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Sistema solare");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new SistemaSolare());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
