package it.unibs.pajc.redolfi.e08;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * Problema 9.8 - l'area della Lavagna completa.
 *
 * Rispetto al capitolo 8: ogni cambiamento del disegno passa da esegui(Modifica), che la fa,
 * la mette nella pila delle modifiche fatte e svuota quella delle disfatte; annulla() e
 * ripeti() spostano le modifiche fra le due pile. Con Maiuscole premuto, trascinare sposta
 * la figura sotto il puntatore: durante il trascinamento la lista cambia direttamente (per
 * vederla muoversi), al rilascio lo spostamento diventa una Modifica. La figura sotto il
 * puntatore e' evidenziata con un tratteggio che scorre: un timer fa avanzare la fase, ed e'
 * acceso solo finche' c'e' una figura evidenziata. immagine() disegna le figure in un
 * BufferedImage con lo stesso codice di paintComponent, senza griglia.
 */
public class PnlLavagna extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color ASSE = new Color(0x9A, 0xA6, 0xB6), RIGA = new Color(0xDD, 0xE3, 0xEC);

    private final List<Figura> figure = new ArrayList<>();
    private final Deque<Modifica> fatte = new ArrayDeque<>(), disfatte = new ArrayDeque<>();
    private final List<Point2D> gesto = new ArrayList<>();
    private Forma forma = Forma.RETTANGOLO;
    private Color colore = Color.BLACK;
    private float spessore = 3;
    private boolean griglia;
    private int passo = 20;
    private double zoom = 1, panX, panY;
    private Point2D puntatore;

    private int evidenziata = -1;                       // indice della figura sotto il mouse
    private int afferrata = -1;                         // indice della figura che si sposta
    private Figura primaDelloSpostamento;
    private Point2D presa;
    private float fase;
    private final Timer formiche = new Timer(60, e -> { fase = (fase + 1) % 16; repaint(); });

    private int visibili = -1;
    private final Timer riproduzione = new Timer(400, e -> stepNext());
    private final EventListenerList listeners = new EventListenerList();

    public PnlLavagna() {
        setBackground(Color.WHITE);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (riproduzione.isRunning())
                    return;
                if (SwingUtilities.isRightMouseButton(e)) {
                    int i = indiceIn(e.getPoint());
                    if (i >= 0)
                        esegui(new Modifica.Cancellazione(i, figure.get(i)));
                    return;
                }
                if (e.isShiftDown()) {                  // si afferra una figura
                    afferrata = indiceIn(e.getPoint());
                    if (afferrata >= 0) {
                        primaDelloSpostamento = figure.get(afferrata);
                        presa = screenToWorld(e.getPoint());
                    }
                    return;
                }
                gesto.clear();
                gesto.add(screenToWorld(e.getPoint()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                aggiornaPuntatore(e.getPoint());
                Point2D p = screenToWorld(e.getPoint());
                if (afferrata >= 0) {
                    figure.set(afferrata, primaDelloSpostamento.spostata(p.getX() - presa.getX(), p.getY() - presa.getY()));
                    repaint();
                } else if (!gesto.isEmpty()) {
                    gesto.add(p);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (afferrata >= 0) {
                    Figura dopo = figure.get(afferrata);
                    if (!dopo.equals(primaDelloSpostamento))
                        esegui(new Modifica.Spostamento(afferrata, primaDelloSpostamento, dopo));
                    afferrata = -1;
                } else if (gesto.size() > 1) {
                    esegui(new Modifica.Aggiunta(figure.size(), new Figura(forma, colore, spessore, gesto)));
                }
                gesto.clear();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                aggiornaPuntatore(e.getPoint());
                evidenzia(indiceIn(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                aggiornaPuntatore(null);
                evidenzia(-1);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double fattore = e.getWheelRotation() < 0 ? 2 : 0.5;
                zoomAttorno(Math.max(0.25, Math.min(8, zoom * fattore)), e.getPoint());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "annullaGesto");
        getActionMap().put("annullaGesto", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { gesto.clear(); repaint(); }
        });
    }

    // ---------------------------------------------------------------- modifiche e storia

    private void esegui(Modifica m) {
        m.fai(figure);
        fatte.push(m);
        disfatte.clear();                               // una modifica nuova: niente da ripetere
        evidenziata = -1;
        fireStateChanged();
        repaint();
    }

    public boolean puoAnnullare() { return !fatte.isEmpty() && !riproduzione.isRunning(); }

    public boolean puoRipetere() { return !disfatte.isEmpty() && !riproduzione.isRunning(); }

    public void annulla() {
        if (!puoAnnullare())
            return;
        Modifica m = fatte.pop();
        m.disfa(figure);
        disfatte.push(m);
        fireStateChanged();
        repaint();
    }

    public void ripeti() {
        if (!puoRipetere())
            return;
        Modifica m = disfatte.pop();
        m.fai(figure);
        fatte.push(m);
        fireStateChanged();
        repaint();
    }

    private void evidenzia(int i) {
        if (i == evidenziata)
            return;
        evidenziata = i;
        if (i >= 0)
            formiche.start();
        else
            formiche.stop();
        repaint();
    }

    // ---------------------------------------------------------------- stato

    public void setForma(Forma forma) { this.forma = forma; }
    public void setColore(Color colore) { this.colore = colore; }
    public void setSpessore(float spessore) { this.spessore = spessore; }
    public double getZoom() { return zoom; }
    public Point2D getPuntatore() { return puntatore; }

    public void setGriglia(boolean griglia, int passo) {
        this.griglia = griglia;
        this.passo = passo;
        repaint();
    }

    public List<Figura> getFigure() { return List.copyOf(figure); }

    /** Un disegno nuovo (letto da file, o vuoto): niente storia da annullare. */
    public void setFigure(List<Figura> nuove) {
        figure.clear();
        figure.addAll(nuove);
        fatte.clear();
        disfatte.clear();
        evidenzia(-1);
        repaint();
    }

    // ---------------------------------------------------------------- vista

    public AffineTransform worldToScreen() {
        AffineTransform t = new AffineTransform();
        t.translate(getWidth() / 2.0 + panX, getHeight() / 2.0 + panY);
        t.scale(zoom, zoom);
        return t;
    }

    public Point2D screenToWorld(Point p) {
        try {
            return worldToScreen().inverseTransform(p, null);
        } catch (NoninvertibleTransformException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setZoom(double zoom) {
        zoomAttorno(zoom, new Point(getWidth() / 2, getHeight() / 2));
    }

    public void zoomAttorno(double nuovo, Point fulcro) {
        Point2D p = screenToWorld(fulcro);
        double vecchio = zoom;
        zoom = nuovo;
        panX = fulcro.x - getWidth() / 2.0 - zoom * p.getX();
        panY = fulcro.y - getHeight() / 2.0 - zoom * p.getY();
        firePropertyChange("zoom", vecchio, zoom);
        repaint();
    }

    private void aggiornaPuntatore(Point schermo) {
        Point2D vecchio = puntatore;
        puntatore = schermo == null ? null : screenToWorld(schermo);
        firePropertyChange("puntatore", vecchio, puntatore);
    }

    private int indiceIn(Point schermo) {
        Point2D p = screenToWorld(schermo);
        for (int i = figure.size() - 1; i >= 0; i--) {
            Stroke largo = new BasicStroke(figure.get(i).spessore() + 6);
            if (largo.createStrokedShape(figure.get(i).forma()).contains(p))
                return i;
        }
        return -1;
    }

    // ---------------------------------------------------------------- riproduzione

    public void riproduci() {
        if (figure.isEmpty() || riproduzione.isRunning())
            return;
        visibili = 0;
        riproduzione.start();
        firePropertyChange("riproduzione", false, true);   // annulla e ripeti si disabilitano
        repaint();
    }

    private void stepNext() {
        visibili++;
        if (visibili >= figure.size()) {
            riproduzione.stop();
            visibili = -1;
            firePropertyChange("riproduzione", true, false);
        }
        repaint();
    }

    // ---------------------------------------------------------------- disegno

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.transform(worldToScreen());
            if (griglia)
                disegnaGriglia(g2);
            disegnaFigure(g2);
            if (gesto.size() > 1)
                disegna(g2, new Figura(forma, colore, spessore, gesto));
            if (evidenziata >= 0 && evidenziata < figure.size() && visibili < 0) {
                Figura f = figure.get(evidenziata);
                Shape alone = new BasicStroke(f.spessore() + 6).createStrokedShape(f.forma());
                g2.setColor(new Color(0x24, 0x54, 0x9C));
                g2.setStroke(new BasicStroke((float) (1 / zoom), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10, new float[] { (float) (5 / zoom), (float) (3 / zoom) }, (float) (fase / zoom)));
                g2.draw(alone);
            }
        } finally {
            g2.dispose();
        }
    }

    private void disegnaFigure(Graphics2D g2) {
        int n = visibili < 0 ? figure.size() : visibili;
        for (Figura f : figure.subList(0, n))
            disegna(g2, f);
    }

    private void disegnaGriglia(Graphics2D g2) {
        Point2D a = screenToWorld(new Point(0, 0)), b = screenToWorld(new Point(getWidth(), getHeight()));
        g2.setStroke(new BasicStroke((float) (1 / zoom)));
        for (double x = Math.floor(a.getX() / passo) * passo; x <= b.getX(); x += passo) {
            g2.setColor(x == 0 ? ASSE : RIGA);
            g2.draw(new Line2D.Double(x, a.getY(), x, b.getY()));
        }
        for (double y = Math.floor(a.getY() / passo) * passo; y <= b.getY(); y += passo) {
            g2.setColor(y == 0 ? ASSE : RIGA);
            g2.draw(new Line2D.Double(a.getX(), y, b.getX(), y));
        }
    }

    static void disegna(Graphics2D g2, Figura f) {
        g2.setColor(f.colore());
        g2.setStroke(new BasicStroke(f.spessore(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(f.forma());
    }

    /** Il disegno come immagine, grande quanto l'area e con la stessa vista, senza griglia. */
    public BufferedImage immagine() {
        BufferedImage img = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.transform(worldToScreen());
            for (Figura f : figure)
                disegna(g2, f);
        } finally {
            g2.dispose();
        }
        return img;
    }

    // ---------------------------------------------------------------- evento

    public void addChangeListener(ChangeListener l) { listeners.add(ChangeListener.class, l); }

    public void removeChangeListener(ChangeListener l) { listeners.remove(ChangeListener.class, l); }

    protected void fireStateChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners.getListeners(ChangeListener.class))
            l.stateChanged(e);
    }
}
