package it.unibs.pajc.redolfi.e04;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/** La vista di Palline: disegna il mondo com'e' adesso, e non lo tocca. */
public class PnlMondo extends JPanel {

    private static final int BORDO = 10;          // pixel attorno al mondo
    private final Mondo mondo;

    public PnlMondo(Mondo mondo) {
        this.mondo = mondo;
        setBackground(new Color(0x101820));
    }

    /** Pixel per metro: il mondo intero sta nel pannello, senza deformarlo. */
    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(),
                        (getHeight() - 2 * BORDO) / mondo.altezza());
    }

    private double worldToScreenX(double x) {
        return BORDO + x * scala();
    }

    private double worldToScreenY(double y) {
        return getHeight() - BORDO - y * scala();
    }

    /** Dal pixel al punto del mondo: serve a chi riceve il mouse. */
    public Vec2 screenToWorld(Point p) {
        return new Vec2((p.x - BORDO) / scala(),
                        (getHeight() - BORDO - p.y) / scala());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(0)));
        g2.setColor(new Color(0xE0A020));
        for (Pallina p : mondo.palline()) {
            double r = p.raggio() * scala();
            g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - r,
                                         worldToScreenY(p.posizione().y()) - r, 2 * r, 2 * r));
        }
    }
}
