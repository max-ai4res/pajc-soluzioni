package it.unibs.pajc.redolfi.e04;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/** La vista di Palline: disegna il mondo e trasforma il click in un evento in metri. */
public class PnlMondo extends JPanel {

    /** Chi vuole sapere dove l'utente ha cliccato, in coordinate del mondo. */
    public interface PuntoListener {
        void puntoScelto(Vec2 punto);
    }

    private static final int BORDO = 10;
    private final Mondo mondo;
    private final List<PuntoListener> listeners = new ArrayList<>();

    public PnlMondo(Mondo mondo) {
        this.mondo = mondo;
        setBackground(new Color(0x101820));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Vec2 p = screenToWorld(e.getPoint());
                for (PuntoListener l : List.copyOf(listeners))
                    l.puntoScelto(p);
            }
        });
    }

    public void addPuntoListener(PuntoListener l)    { listeners.add(l); }
    public void removePuntoListener(PuntoListener l) { listeners.remove(l); }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(),
                        (getHeight() - 2 * BORDO) / mondo.altezza());
    }

    private double worldToScreenX(double x) { return BORDO + x * scala(); }
    private double worldToScreenY(double y) { return getHeight() - BORDO - y * scala(); }

    private Vec2 screenToWorld(Point p) {
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
        for (Pallina p : mondo.palline()) {
            double r = p.raggio() * scala();
            g2.setColor(p.ferma() ? new Color(0x8A6A20) : new Color(0xE0A020));
            g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - r,
                                         worldToScreenY(p.posizione().y()) - r, 2 * r, 2 * r));
        }
        if (mondo.inPausa()) {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setFont(getFont().deriveFont(28f));
            g2.drawString("pausa", getWidth() / 2 - 40, getHeight() / 2);
        }
    }
}
