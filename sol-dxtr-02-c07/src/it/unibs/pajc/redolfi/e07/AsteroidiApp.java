package it.unibs.pajc.redolfi.e07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Problema 7, Asteroidi.
 *
 * Idea: le entita' estendono Entita (Template Method: avanza fissa il moto e il toro,
 * primaDelMoto e dopoIlMoto sono i ganci); l'invulnerabilita' e' uno stato della navicella
 * con un tempo che si spegne da solo in dopoIlMoto; i proiettili da un pool; le collisioni
 * nel modello con i cerchi; la vista disegna le forme vere con Path2D e AffineTransform;
 * i renderer sono una Strategy scelta con uno switch sui tipi (l'insieme e' chiuso: tre
 * tipi, e il compilatore avvisa se se ne aggiunge uno); punteggio e vite come proprieta'.
 *
 * Errore tipico: l'invulnerabilita' come booleano acceso alla rinascita e mai spento:
 * navicella immortale. ProvaUniverso fa scontrare la navicella a 0, 2 e 4 secondi dalla
 * rinascita e verifica che conti solo l'ultimo.
 */
public class AsteroidiApp {

    interface Stato {
        default void stepNext(AsteroidiApp app, double dt) { }
        default void tasto(AsteroidiApp app, int c) { }
        String messaggio();
    }

    static class Menu implements Stato {
        public void tasto(AsteroidiApp app, int c) { if (c == KeyEvent.VK_SPACE) app.cambia(new InCorso()); }
        public String messaggio() { return "Asteroidi  -  frecce e spazio"; }
    }

    static class InCorso implements Stato {
        public void stepNext(AsteroidiApp app, double dt) {
            app.universo.navicella().comanda((app.premuti.contains(KeyEvent.VK_LEFT) ? 1 : 0) - (app.premuti.contains(KeyEvent.VK_RIGHT) ? 1 : 0),
                    app.premuti.contains(KeyEvent.VK_UP) ? 1 : 0);
            app.universo.stepNext(dt);
            if (app.universo.vite() <= 0)
                app.cambia(new Finita());
        }
        public void tasto(AsteroidiApp app, int c) {
            if (c == KeyEvent.VK_SPACE) app.universo.spara();
            if (c == KeyEvent.VK_P) app.cambia(new Pausa());
        }
        public String messaggio() { return null; }
    }

    static class Pausa implements Stato {
        public void tasto(AsteroidiApp app, int c) { if (c == KeyEvent.VK_P) app.cambia(new InCorso()); }
        public String messaggio() { return "pausa (P)"; }
    }

    static class Finita implements Stato {
        public String messaggio() { return "fine"; }
    }

    // ---- i renderer: una Strategy per tipo
    static final Renderer<Navicella> NAVICELLA = (g2, n, s) -> {
        Path2D.Double forma = new Path2D.Double();
        forma.moveTo(0.6, 0);
        forma.lineTo(-0.45, 0.4);
        forma.lineTo(-0.25, 0);
        forma.lineTo(-0.45, -0.4);
        forma.closePath();
        g2.setColor(n.invulnerabile() && (int) (n.eta() * 8) % 2 == 0 ? new Color(0x806030) : new Color(0xE0A020));
        g2.fill(trasforma(n, s).createTransformedShape(forma));
    };
    static final Renderer<Asteroide> ASTEROIDE = (g2, a, s) -> {
        Path2D.Double forma = new Path2D.Double();
        for (int i = 0; i < 9; i++) {
            double ang = i * 2 * Math.PI / 9, r = a.raggio() * (0.75 + 0.25 * Math.sin(i * 2.7));
            if (i == 0) forma.moveTo(r * Math.cos(ang), r * Math.sin(ang)); else forma.lineTo(r * Math.cos(ang), r * Math.sin(ang));
        }
        forma.closePath();
        g2.setColor(new Color(0x9AA6B6));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(trasforma(a, s).createTransformedShape(forma));
    };
    static final Renderer<Proiettile> PROIETTILE = (g2, p, s) -> {
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(p.posizione().x() * s - 3, (Universo.ALTEZZA - p.posizione().y()) * s - 3, 6, 6));
    };

    static AffineTransform trasforma(Entita e, double s) {
        AffineTransform t = new AffineTransform();
        t.translate(e.posizione().x() * s, (Universo.ALTEZZA - e.posizione().y()) * s);
        t.scale(s, -s);
        t.rotate(e.angolo());
        return t;
    }

    Universo universo = new Universo(System.nanoTime());
    Stato stato;
    final Set<Integer> premuti = new HashSet<>();
    final JPanel vista = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double s = Math.min(getWidth() / Universo.LARGHEZZA, getHeight() / Universo.ALTEZZA);
            for (Asteroide a : universo.asteroidi())
                ASTEROIDE.disegna(g2, a, s);
            for (Proiettile p : universo.proiettili())
                if (p.viva())
                    PROIETTILE.disegna(g2, p, s);
            NAVICELLA.disegna(g2, universo.navicella(), s);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("punti " + universo.punteggio() + "   vite " + universo.vite(), 10, 20);
            if (stato.messaggio() != null) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
                String m = stato instanceof Finita ? "fine: " + universo.punteggio() + " punti" : stato.messaggio();
                g2.drawString(m, (getWidth() - g2.getFontMetrics().stringWidth(m)) / 2, getHeight() / 2);
            }
        }
    };

    void cambia(Stato nuovo) {
        stato = nuovo;
        vista.repaint();
    }

    JFrame finestra() {
        cambia(new Menu());
        vista.setBackground(new Color(0x05080E));
        vista.setFocusable(true);
        vista.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e)  { premuti.add(e.getKeyCode()); stato.tasto(AsteroidiApp.this, e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { premuti.remove(e.getKeyCode()); }
        });
        JFrame f = new JFrame("Asteroidi");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(vista);
        f.setSize(960, 660);
        long[] ultimo = { System.nanoTime() };
        new Timer(16, e -> {
            long adesso = System.nanoTime();
            stato.stepNext(this, (adesso - ultimo[0]) / 1e9);
            ultimo[0] = adesso;
            vista.repaint();
        }).start();
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AsteroidiApp app = new AsteroidiApp();
            app.finestra().setVisible(true);
            app.vista.requestFocusInWindow();
        });
    }
}
