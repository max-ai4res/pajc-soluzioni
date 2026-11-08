package it.unibs.pajc.redolfi.e05;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/** Il tavolo con le buche, la stecca: come PnlBiliardo del capitolo 4, ma parla con la Partita. */
public class PnlTavolo extends JPanel {

    private static final int BORDO = 24;
    private final Partita partita;
    private Point inizio, corrente;

    public PnlTavolo(Partita partita) {
        this.partita = partita;
        setBackground(new Color(0x1E5A3A));
        MouseAdapter stecca = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (partita.stato() instanceof Partita.TurnoDi
                        && screenToWorld(e.getPoint()).meno(partita.bianca().posizione()).modulo() < 0.1)
                    inizio = corrente = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (inizio != null) { corrente = e.getPoint(); repaint(); }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (inizio != null) {
                    partita.colpo(screenToWorld(inizio).meno(screenToWorld(e.getPoint())).per(4));
                    inizio = corrente = null;
                }
            }
        };
        addMouseListener(stecca);
        addMouseMotionListener(stecca);
    }

    private double scala() {
        Mondo m = partita.mondo();
        return Math.min((getWidth() - 2 * BORDO) / m.larghezza(), (getHeight() - 2 * BORDO) / m.altezza());
    }

    private double worldToScreenX(double x) { return BORDO + x * scala(); }
    private double worldToScreenY(double y) { return getHeight() - BORDO - y * scala(); }

    private Vec2 screenToWorld(Point p) {
        return new Vec2((p.x - BORDO) / scala(), (getHeight() - BORDO - p.y) / scala());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Mondo m = partita.mondo();
        double s = scala();
        g2.setColor(new Color(0x5A3A1E));
        g2.setStroke(new BasicStroke(8f));
        g2.drawRect((int) worldToScreenX(0) - 4, (int) worldToScreenY(m.altezza()) - 4, (int) (m.larghezza() * s) + 8, (int) (m.altezza() * s) + 8);
        g2.setColor(Color.BLACK);
        for (Vec2 b : m.buche()) {
            double r = 0.07 * s;
            g2.fill(new Ellipse2D.Double(worldToScreenX(b.x()) - r, worldToScreenY(b.y()) - r, 2 * r, 2 * r));
        }
        for (Pallina p : m.palline()) {
            double r = p.raggio() * s;
            g2.setColor(p == partita.bianca() ? Color.WHITE : new Color(0xC94F2E));
            g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - r, worldToScreenY(p.posizione().y()) - r, 2 * r, 2 * r));
        }
        if (inizio != null) {
            g2.setColor(new Color(0xE0C080));
            g2.setStroke(new BasicStroke(4f));
            Vec2 b = partita.bianca().posizione();
            g2.draw(new Line2D.Double(worldToScreenX(b.x()), worldToScreenY(b.y()), corrente.x, corrente.y));
        }
    }
}
