package it.unibs.pajc.redolfi.e02;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/** La vista: come PnlMondo del capitolo, piu le rampe; il click lascia una pallina. */
public class PnlRampa extends JPanel {

    private static final int BORDO = 10;
    private final Mondo mondo;

    public PnlRampa(Mondo mondo) {
        this.mondo = mondo;
        setBackground(new Color(0x101820));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mondo.lascia(new Vec2((e.getX() - BORDO) / scala(), (getHeight() - BORDO - e.getY()) / scala()), 0.2);
            }
        });
    }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(), (getHeight() - 2 * BORDO) / mondo.altezza());
    }

    private double worldToScreenX(double x) { return BORDO + x * scala(); }
    private double worldToScreenY(double y) { return getHeight() - BORDO - y * scala(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(0)));
        g2.setStroke(new BasicStroke(3f));
        for (Rampa r : mondo.rampe())
            g2.draw(new Line2D.Double(worldToScreenX(r.a().x()), worldToScreenY(r.a().y()), worldToScreenX(r.b().x()), worldToScreenY(r.b().y())));
        for (Pallina p : mondo.palline()) {
            double rr = p.raggio() * scala();
            g2.setColor(p.ferma() ? new Color(0x8A6A20) : new Color(0xE0A020));
            g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - rr, worldToScreenY(p.posizione().y()) - rr, 2 * rr, 2 * rr));
        }
    }
}
