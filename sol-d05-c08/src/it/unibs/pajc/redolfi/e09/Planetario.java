package it.unibs.pajc.redolfi.e09;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 8.9 - il planetario animato.
 *
 * Ogni corpo ha una velocita' angolare (radianti al secondo del tempo simulato). Il timer
 * scatta ogni 16 ms, ma lo spostamento si calcola dal tempo VERO trascorso dall'ultimo
 * scatto (System.nanoTime), moltiplicato per il fattore di velocita': se il timer rallenta,
 * i pianeti non rallentano. I tasti sono legati con InputMap e ActionMap (spazio, + e -),
 * cosi' funzionano senza preoccuparsi del fuoco. Il disegno e' quello dell'esercizio 7.8:
 * copie del contesto, rotate e translate annidati.
 *
 * Errore tipico: aumentare l'angolo di una quantita' fissa a ogni scatto, e una velocita'
 * che dipende dal computer.
 */
public class Planetario extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final double[] DISTANZA = { 1.0, 1.8, 3.0 };
    private static final double[] RAGGIO = { 0.10, 0.16, 0.20 };
    private static final double[] VELOCITA = { 1.6, 0.9, 0.45 };      // i lontani piu' lenti
    private static final Color[] COLORE = { new Color(0xB0, 0x8D, 0x57), new Color(0xE0, 0x8A, 0x1E),
            new Color(0x24, 0x54, 0x9C) };

    private final double[] angolo = { 0.5, 2.8, -1.0 };
    private double angoloLuna;
    private double fattore = 1;
    private long ultimo = System.nanoTime();
    private final Timer timer = new Timer(16, e -> stepNext());

    public Planetario() {
        setBackground(new Color(0x10, 0x18, 0x20));
        setPreferredSize(new Dimension(560, 560));
        lega(KeyEvent.VK_SPACE, "pausa", () -> {
            if (timer.isRunning()) timer.stop();
            else { ultimo = System.nanoTime(); timer.start(); }
        });
        lega(KeyEvent.VK_PLUS, "piu", () -> fattore *= 1.5);
        lega(KeyEvent.VK_ADD, "piu", () -> fattore *= 1.5);
        lega(KeyEvent.VK_MINUS, "meno", () -> fattore /= 1.5);
        lega(KeyEvent.VK_SUBTRACT, "meno", () -> fattore /= 1.5);
        timer.start();
    }

    private void lega(int tasto, String nome, Runnable azione) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tasto, 0), nome);
        getActionMap().put(nome, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { azione.run(); }
        });
    }

    private void stepNext() {
        long ora = System.nanoTime();
        double dt = (ora - ultimo) / 1e9 * fattore;       // secondi simulati
        ultimo = ora;
        for (int i = 0; i < angolo.length; i++)
            angolo[i] += VELOCITA[i] * dt;
        angoloLuna += 4.0 * dt;
        repaint();
    }

    private static Ellipse2D cerchio(double r) {
        return new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scala = Math.min(getWidth(), getHeight()) / 2.0 / 3.5;
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(scala, scala);
            g2.setStroke(new BasicStroke((float) (1 / scala)));
            g2.setColor(new Color(0xFF, 0xD2, 0x4A));
            g2.fill(cerchio(0.35));
            for (int i = 0; i < angolo.length; i++) {
                g2.setColor(new Color(0x40, 0x4C, 0x5C));
                g2.draw(cerchio(DISTANZA[i]));
                Graphics2D gp = (Graphics2D) g2.create();
                gp.rotate(angolo[i]);
                gp.translate(DISTANZA[i], 0);
                gp.setColor(COLORE[i]);
                gp.fill(cerchio(RAGGIO[i]));
                if (i == angolo.length - 1) {
                    gp.rotate(angoloLuna);
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
            JFrame f = new JFrame("Planetario  (spazio: pausa, + e -: velocita')");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Planetario());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
