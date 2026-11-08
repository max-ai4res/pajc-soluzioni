package it.unibs.pajc.redolfi.e04;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/** La vista con il tema: una proprieta' "tema" annunciata, e un Disegnatore che la applica. */
public class PnlMondo extends JPanel {

    public interface PuntoListener {
        void puntoScelto(Vec2 punto);
    }

    private static final int BORDO = 10;
    private Mondo mondo;
    private String messaggio;
    private Disegnatore disegnatore = new DisegnatoreTema(Tema.daNome("classico"));
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);
    private final List<PuntoListener> listeners = new ArrayList<>();

    public PnlMondo(Mondo mondo) {
        this.mondo = mondo;
        setBackground(disegnatore.tema().sfondo());
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

    public Tema tema() { return disegnatore.tema(); }

    /** Il tema cambia a caldo: nessuna pallina e nessun controllore se ne accorgono. */
    public void setTema(Tema t) {
        Tema vecchio = disegnatore.tema();
        disegnatore = new DisegnatoreTema(t);
        setBackground(t.sfondo());
        proprieta.firePropertyChange("tema", vecchio, t);
        repaint();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) { proprieta.addPropertyChangeListener(l); }
    public void addPuntoListener(PuntoListener l)    { listeners.add(l); }
    public void removePuntoListener(PuntoListener l) { listeners.remove(l); }

    private double scala() {
        return Math.min((getWidth() - 2 * BORDO) / mondo.larghezza(), (getHeight() - 2 * BORDO) / mondo.altezza());
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
        double s = scala();
        Tema tema = disegnatore.tema();
        g2.setColor(tema.pavimento());
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(0)));
        g2.draw(new Line2D.Double(worldToScreenX(0), worldToScreenY(0), worldToScreenX(0), worldToScreenY(mondo.altezza())));
        g2.draw(new Line2D.Double(worldToScreenX(mondo.larghezza()), worldToScreenY(0), worldToScreenX(mondo.larghezza()), worldToScreenY(mondo.altezza())));
        for (Ostacolo o : mondo.ostacoli())
            g2.fill(new Rectangle2D.Double(worldToScreenX(o.x()), worldToScreenY(o.y() + o.altezza()), o.larghezza() * s, o.altezza() * s));
        for (Pallina p : mondo.palline())
            disegnatore.pallina(g2, p, worldToScreenX(p.posizione().x()), worldToScreenY(p.posizione().y()), p.raggio() * s);
        for (Particella sch : mondo.particelle())
            if (sch.viva())
                disegnatore.scheggia(g2, sch, worldToScreenX(sch.posizione().x()), worldToScreenY(sch.posizione().y()), 0.03 * s);
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
