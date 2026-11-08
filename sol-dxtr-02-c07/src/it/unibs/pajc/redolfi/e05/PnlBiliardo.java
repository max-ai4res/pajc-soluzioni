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

/** La vista del tavolo e, insieme, il gesto della stecca: dalla bianca si trascina e si rilascia. */
public class PnlBiliardo extends JPanel {

    private static final int BORDO = 20;
    private final Mondo mondo;
    private final Pallina bianca;
    private Point inizio, corrente;

    public PnlBiliardo(Mondo mondo, Pallina bianca) {
        this.mondo = mondo;
        this.bianca = bianca;
        setBackground(new Color(0x1E5A3A));
        MouseAdapter stecca = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (mondo.tutteFerme() && screenToWorld(e.getPoint()).meno(bianca.posizione()).modulo() < 3 * bianca.raggio())
                    inizio = corrente = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (inizio != null) {
                    corrente = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (inizio != null) {
                    Vec2 tiro = screenToWorld(inizio).meno(screenToWorld(e.getPoint())).per(4);   // la stecca spinge nel verso opposto
                    mondo.colpisci(bianca, tiro);
                    inizio = corrente = null;
                }
            }
        };
        addMouseListener(stecca);
        addMouseMotionListener(stecca);
    }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(), (getHeight() - 2 * BORDO) / mondo.altezza());
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
        g2.setColor(new Color(0x5A3A1E));
        g2.setStroke(new BasicStroke(6f));
        g2.drawRect((int) worldToScreenX(0) - 3, (int) worldToScreenY(mondo.altezza()) - 3, (int) (mondo.larghezza() * scala()) + 6, (int) (mondo.altezza() * scala()) + 6);
        for (Pallina p : mondo.palline()) {
            double r = p.raggio() * scala();
            g2.setColor(p == bianca ? Color.WHITE : new Color(0xC94F2E));
            g2.fill(new Ellipse2D.Double(worldToScreenX(p.posizione().x()) - r, worldToScreenY(p.posizione().y()) - r, 2 * r, 2 * r));
        }
        if (inizio != null) {
            g2.setColor(new Color(0xE0C080));
            g2.setStroke(new BasicStroke(4f));
            g2.draw(new Line2D.Double(worldToScreenX(bianca.posizione().x()), worldToScreenY(bianca.posizione().y()), corrente.x, corrente.y));
        }
        if (!mondo.tutteFerme()) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("in movimento...", 10, getHeight() - 6);
        }
    }
}
