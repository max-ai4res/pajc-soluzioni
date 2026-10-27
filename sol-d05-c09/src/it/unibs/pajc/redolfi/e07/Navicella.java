package it.unibs.pajc.redolfi.e07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Problema 9.7 - la navicella.
 *
 * I tasti premuti stanno in un insieme (keyPressed aggiunge, keyReleased toglie, focusLost
 * svuota); il timer, ogni 16 ms, legge l'insieme, ruota e spinge la navicella, sposta gli
 * asteroidi e controlla le collisioni. La forma della navicella in posizione e' il Path2D
 * di base trasformato con traslazione e rotazione; la collisione si verifica intersecando
 * le Area. Alla collisione il timer si ferma PRIMA della domanda modale: durante la domanda
 * il ciclo secondario continua a distribuire eventi, e un timer acceso aprirebbe altre
 * domande. Il rientro dai bordi e' un modulo sulla dimensione del pannello.
 *
 * Errore tipico: muovere la navicella dentro keyPressed, che dipende dalla ripetizione
 * automatica e non funziona con due tasti insieme.
 */
public class Navicella extends JPanel {

    private static final long serialVersionUID = 1L;

    static class Asteroide {
        double x, y, vx, vy, r;
    }

    private static final Path2D FORMA = new Path2D.Double();
    static {                                            // la navicella, con la punta verso destra
        FORMA.moveTo(18, 0);
        FORMA.lineTo(-12, -11);
        FORMA.lineTo(-6, 0);
        FORMA.lineTo(-12, 11);
        FORMA.closePath();
    }

    private final Set<Integer> premuti = new HashSet<>();
    private final List<Asteroide> asteroidi = new ArrayList<>();
    private final Random caso = new Random();
    private final Timer timer = new Timer(16, e -> stepNext());
    private double x, y, angolo, vx, vy;
    private long inizio;

    public Navicella() {
        setBackground(new Color(0x08, 0x0C, 0x14));
        setPreferredSize(new Dimension(800, 560));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { premuti.add(e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { premuti.remove(e.getKeyCode()); }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { premuti.clear(); }
        });
    }

    void nuovaPartita() {
        x = getWidth() / 2.0;
        y = getHeight() / 2.0;
        angolo = -Math.PI / 2;
        vx = vy = 0;
        asteroidi.clear();
        for (int i = 0; i < 7; i++) {
            Asteroide a = new Asteroide();
            a.r = 15 + caso.nextInt(35);
            a.x = caso.nextBoolean() ? 0 : getWidth();          // dai bordi, lontano dal centro
            a.y = caso.nextInt(Math.max(1, getHeight()));
            double dir = caso.nextDouble() * 2 * Math.PI, v = 1 + caso.nextDouble() * 2;
            a.vx = v * Math.cos(dir);
            a.vy = v * Math.sin(dir);
            asteroidi.add(a);
        }
        inizio = System.nanoTime();
        premuti.clear();
        requestFocusInWindow();
        timer.start();
    }

    private Shape formaNavicella() {
        AffineTransform t = new AffineTransform();
        t.translate(x, y);
        t.rotate(angolo);
        return t.createTransformedShape(FORMA);
    }

    private double avvolgi(double v, double max) {
        return ((v % max) + max) % max;
    }

    private void stepNext() {
        if (premuti.contains(KeyEvent.VK_LEFT)) angolo -= 0.07;
        if (premuti.contains(KeyEvent.VK_RIGHT)) angolo += 0.07;
        if (premuti.contains(KeyEvent.VK_UP)) {
            vx += 0.18 * Math.cos(angolo);
            vy += 0.18 * Math.sin(angolo);
        }
        vx *= 0.99;                                     // un po' d'attrito
        vy *= 0.99;
        x = avvolgi(x + vx, getWidth());
        y = avvolgi(y + vy, getHeight());
        Area nave = new Area(formaNavicella());
        for (Asteroide a : asteroidi) {
            a.x = avvolgi(a.x + a.vx, getWidth());
            a.y = avvolgi(a.y + a.vy, getHeight());
            Area tocco = new Area(new Ellipse2D.Double(a.x - a.r, a.y - a.r, 2 * a.r, 2 * a.r));
            tocco.intersect(nave);
            if (!tocco.isEmpty()) {
                fine();
                return;
            }
        }
        repaint();
    }

    private void fine() {
        timer.stop();                                   // prima della domanda modale
        repaint();
        double secondi = (System.nanoTime() - inizio) / 1e9;
        int r = JOptionPane.showConfirmDialog(this, String.format("Colpito dopo %.1f secondi. Ricominciare?", secondi),
                "Navicella", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION)
            nuovaPartita();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(0x9A, 0xA6, 0xB6));
            for (Asteroide a : asteroidi)
                g2.draw(new Ellipse2D.Double(a.x - a.r, a.y - a.r, 2 * a.r, 2 * a.r));
            g2.setColor(new Color(0xE0, 0x8A, 0x1E));
            g2.fill(formaNavicella());
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Navicella  (frecce: ruota e spingi)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Navicella n = new Navicella();
            f.add(n);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            n.nuovaPartita();                           // dopo pack: il pannello ha una dimensione
        });
    }
}
