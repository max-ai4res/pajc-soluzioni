package it.unibs.pajc.redolfi.e02;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/** La vista: i due semafori, letti dal modello quando si disegna. */
public class PnlSemaforo extends JPanel {

    private final Semaforo semaforo;

    public PnlSemaforo(Semaforo semaforo) {
        this.semaforo = semaforo;
        setBackground(new Color(0x202830));
    }

    private void luce(Graphics2D g2, double x, double y, double d, Color colore, boolean accesa) {
        g2.setColor(accesa ? colore : colore.darker().darker().darker());
        g2.fill(new Ellipse2D.Double(x, y, d, d));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        double d = Math.min(getWidth() / 6.0, getHeight() / 5.0);   // diametro di una luce
        double x1 = getWidth() / 2.0 - 2.2 * d, x2 = getWidth() / 2.0 + 0.7 * d;
        double y = (getHeight() - 3.6 * d) / 2;

        g2.setColor(Color.DARK_GRAY);                      // le due carcasse
        g2.fill(new RoundRectangle2D.Double(x1 - 0.25 * d, y - 0.25 * d, 1.5 * d, 3.85 * d, d / 3, d / 3));
        g2.fill(new RoundRectangle2D.Double(x2 - 0.25 * d, y - 0.25 * d, 1.5 * d, 2.65 * d, d / 3, d / 3));

        Semaforo.Luce auto = semaforo.luceAuto(), pedoni = semaforo.lucePedoni();
        luce(g2, x1, y, d, Color.RED, auto == Semaforo.Luce.ROSSO);
        luce(g2, x1, y + 1.2 * d, d, Color.YELLOW, auto == Semaforo.Luce.GIALLO);
        luce(g2, x1, y + 2.4 * d, d, Color.GREEN, auto == Semaforo.Luce.VERDE);
        luce(g2, x2, y, d, Color.RED, pedoni == Semaforo.Luce.ROSSO);
        luce(g2, x2, y + 1.2 * d, d, Color.GREEN, pedoni == Semaforo.Luce.VERDE);

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(String.format("%s  %.1f s", semaforo.fase(), semaforo.tempoNellaFase()), 10, getHeight() - 10);
    }
}
