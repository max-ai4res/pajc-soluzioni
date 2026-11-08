package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/** La vista: ogni materiale ha il suo aspetto (uno switch sul sealed), piu le schegge e il messaggio dello stato. */
public class PnlMondo extends JPanel {

    public interface PuntoListener {
        void puntoScelto(Vec2 punto);
    }

    private static final int BORDO = 10;
    private Mondo mondo;
    private String messaggio;
    private final List<PuntoListener> listeners = new ArrayList<>();

    public PnlMondo(Mondo mondo) {
        this.mondo = mondo;
        setBackground(new Color(0x101820));
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Vec2 p = screenToWorld(e.getPoint());
                for (PuntoListener l : List.copyOf(listeners))
                    l.puntoScelto(p);
            }
        });
    }

    public Mondo mondo() { return mondo; }

    void setMondo(Mondo m) { mondo = m; }

    void setMessaggio(String m) { messaggio = m; }

    public void addPuntoListener(PuntoListener l)    { listeners.add(l); }
    public void removePuntoListener(PuntoListener l) { listeners.remove(l); }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(),
                        (getHeight() - 2 * BORDO) / mondo.altezza());
    }

    private double worldToScreenX(double x) { return BORDO + x * scala(); }
    private double worldToScreenY(double y) { return getHeight() - BORDO - y * scala(); }

    private Vec2 screenToWorld(Point p) {
        return new Vec2((p.x - BORDO) / scala(), (getHeight() - BORDO - p.y) / scala());
    }

    /** L'aspetto dipende dal materiale: lo decide la vista, non il materiale. */
    private void disegna(Graphics2D g2, Pallina p) {
        double s = scala(), r = p.raggio() * s;
        double x = worldToScreenX(p.posizione().x()), y = worldToScreenY(p.posizione().y());
        Ellipse2D forma = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
        switch (p.materiale()) {
            case Gomma gomma -> {
                g2.setColor(p.ferma() ? new Color(0x8A6A20) : new Color(0xE0A020));
                g2.fill(forma);
            }
            case Piombo piombo -> {
                g2.setColor(new Color(0x6A7080));
                g2.fill(forma);
                g2.setColor(new Color(0xA0A8B8));
                g2.fill(new Ellipse2D.Double(x - r / 2, y - r / 2, r / 2, r / 3));
            }
            case Bolla bolla -> {
                g2.setColor(new Color(0x60C0FF));
                g2.draw(forma);
                g2.setColor(new Color(0x60C0FF, false).darker());
                g2.draw(new Ellipse2D.Double(x - r * 0.6, y - r * 0.6, r * 0.5, r * 0.5));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double s = scala();
        g2.setColor(Color.GRAY);
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(0)));
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(0), worldToScreenY(mondo.altezza())));
        g2.draw(new Line2D.Double(worldToScreenX(mondo.larghezza()), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(mondo.altezza())));
        g2.setColor(new Color(0x3A4656));
        for (Ostacolo o : mondo.ostacoli())
            g2.fill(new Rectangle2D.Double(worldToScreenX(o.x()), worldToScreenY(o.y() + o.altezza()), o.larghezza() * s, o.altezza() * s));
        for (Pallina p : mondo.palline())
            disegna(g2, p);
        for (Particella sch : mondo.particelle()) {
            if (!sch.viva())
                continue;
            g2.setColor(new Color(255, 220, 120, (int) (200 * sch.vitaResidua())));
            double r = 0.03 * s;
            g2.fill(new Ellipse2D.Double(worldToScreenX(sch.posizione().x()) - r, worldToScreenY(sch.posizione().y()) - r, 2 * r, 2 * r));
        }
        if (messaggio != null) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.BOLD, 26f));
            int w = g2.getFontMetrics().stringWidth(messaggio);
            g2.drawString(messaggio, (getWidth() - w) / 2, getHeight() / 2);
        }
    }
}
