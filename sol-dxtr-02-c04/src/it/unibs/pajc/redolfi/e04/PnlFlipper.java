package it.unibs.pajc.redolfi.e04;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class PnlFlipper extends JPanel {

    private static final int BORDO = 10;
    private final Flipper flipper;

    public PnlFlipper(Flipper flipper) {
        this.flipper = flipper;
        setBackground(new Color(0x101820));
        setFocusable(true);
    }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / Flipper.LARGHEZZA, (getHeight() - 2 * BORDO) / Flipper.ALTEZZA);
    }

    private double worldToScreenX(double x) { return BORDO + x * scala(); }
    private double worldToScreenY(double y) { return getHeight() - BORDO - y * scala(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        double s = scala();
        g2.setColor(Color.GRAY);
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(0), worldToScreenY(Flipper.ALTEZZA)));
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(Flipper.ALTEZZA), worldToScreenX(Flipper.LARGHEZZA), worldToScreenY(Flipper.ALTEZZA)));
        g2.draw(new Line2D.Double(worldToScreenX(Flipper.LARGHEZZA), worldToScreenY(0), worldToScreenX(Flipper.LARGHEZZA), worldToScreenY(Flipper.ALTEZZA)));
        g2.setColor(new Color(0xC94F2E));
        for (Flipper.Respingente r : flipper.respingenti()) {
            double rr = r.raggio() * s;
            g2.fill(new Ellipse2D.Double(worldToScreenX(r.centro().x()) - rr, worldToScreenY(r.centro().y()) - rr, 2 * rr, 2 * rr));
        }
        g2.setColor(new Color(0x2E7D8C));
        g2.setStroke(new BasicStroke((float) (0.12 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Flipper.Aletta a : flipper.alette())
            g2.draw(new Line2D.Double(worldToScreenX(a.perno().x()), worldToScreenY(a.perno().y()), worldToScreenX(a.punta().x()), worldToScreenY(a.punta().y())));
        Pallina p = flipper.pallina();
        double rr = p.raggio() * s;
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - rr, worldToScreenY(p.posizione().y()) - rr, 2 * rr, 2 * rr));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("punti " + flipper.punteggio() + "   palline " + flipper.pallineRimaste() + "   Z e M per le alette", 12, 20);
    }
}
