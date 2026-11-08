package it.unibs.pajc.redolfi.e03;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Problema 3, Pong.
 *
 * Idea: il modello Pong a passo fisso con le racchette come ostacoli che si muovono; la
 * tastiera aggiorna un insieme di tasti premuti e il passo del controllore lo traduce in
 * muovi(versoA, versoB); il punteggio e' una vista separata (due etichette) che ascolta
 * punto(); gli stati Menu, InCorso, Pausa, Finita a 11 punti. ProvaPong verifica il
 * rimbalzo su una racchetta ferma e il punto su un lato scoperto.
 *
 * Errore tipico: muovere la racchetta in keyPressed: a scatti, al ritmo della ripetizione
 * automatica, e con due tasti premuti il secondo blocca il primo.
 */
public class PongApp {

    interface Stato {
        default void stepNext(PongApp app, double dt) { }
        default void tasto(PongApp app, int codice) { }
        String messaggio();
    }

    static class Menu implements Stato {
        public void tasto(PongApp app, int c) { if (c == KeyEvent.VK_SPACE) app.cambia(new InCorso()); }
        public String messaggio() { return "Pong  -  W/S e frecce  -  spazio"; }
    }

    static class InCorso implements Stato {
        public void stepNext(PongApp app, double dt) {
            app.pong.muovi((app.premuti.contains(KeyEvent.VK_W) ? 1 : 0) - (app.premuti.contains(KeyEvent.VK_S) ? 1 : 0),
                    (app.premuti.contains(KeyEvent.VK_UP) ? 1 : 0) - (app.premuti.contains(KeyEvent.VK_DOWN) ? 1 : 0));
            app.pong.stepNext(dt);
            if (app.pong.puntiA() >= 11 || app.pong.puntiB() >= 11)
                app.cambia(new Finita());
        }
        public void tasto(PongApp app, int c) { if (c == KeyEvent.VK_SPACE) app.cambia(new Pausa()); }
        public String messaggio() { return null; }
    }

    static class Pausa implements Stato {
        public void tasto(PongApp app, int c) { if (c == KeyEvent.VK_SPACE) app.cambia(new InCorso()); }
        public String messaggio() { return "pausa"; }
    }

    static class Finita implements Stato {
        public void tasto(PongApp app, int c) {
            if (c == KeyEvent.VK_SPACE) { app.pong = new Pong(); app.collega(); app.cambia(new InCorso()); }
        }
        public String messaggio() { return "fine  -  spazio per ricominciare"; }
    }

    Pong pong = new Pong();
    Stato stato;
    final Set<Integer> premuti = new HashSet<>();
    final JLabel punteggio = new JLabel("0   :   0", SwingConstants.CENTER);
    final JPanel campo = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double s = Math.min(getWidth() / Pong.LARGHEZZA, getHeight() / Pong.ALTEZZA);
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Double((1.0 - Pong.RACCHETTA_W / 2) * s, getHeight() - (pong.yA() + Pong.RACCHETTA_H / 2) * s,
                    Pong.RACCHETTA_W * s, Pong.RACCHETTA_H * s));
            g2.fill(new Rectangle2D.Double((Pong.LARGHEZZA - 1.0 - Pong.RACCHETTA_W / 2) * s,
                    getHeight() - (pong.yB() + Pong.RACCHETTA_H / 2) * s, Pong.RACCHETTA_W * s, Pong.RACCHETTA_H * s));
            double r = Pong.RAGGIO * s;
            g2.fill(new Ellipse2D.Double(pong.pallina().x() * s - r, getHeight() - pong.pallina().y() * s - r, 2 * r, 2 * r));
            if (stato.messaggio() != null) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
                int w = g2.getFontMetrics().stringWidth(stato.messaggio());
                g2.drawString(stato.messaggio(), (getWidth() - w) / 2, getHeight() / 2);
            }
        }
    };

    void collega() {
        pong.addListener(new Pong.Listener() {           // il punteggio ascolta il modello
            @Override
            public void punto(int a, int b) {
                punteggio.setText(a + "   :   " + b);
            }
        });
        punteggio.setText("0   :   0");
    }

    void cambia(Stato nuovo) {
        stato = nuovo;
        campo.repaint();
    }

    JFrame finestra() {
        collega();
        cambia(new Menu());
        punteggio.setFont(punteggio.getFont().deriveFont(Font.BOLD, 36f));
        campo.setBackground(new Color(0x101820));
        campo.setFocusable(true);
        campo.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e)  { premuti.add(e.getKeyCode()); stato.tasto(PongApp.this, e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { premuti.remove(e.getKeyCode()); }
        });
        JFrame f = new JFrame("Pong");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(punteggio, BorderLayout.NORTH);
        f.add(campo, BorderLayout.CENTER);
        f.setSize(800, 580);
        long[] ultimo = { System.nanoTime() };
        new Timer(16, e -> {
            long adesso = System.nanoTime();
            stato.stepNext(this, (adesso - ultimo[0]) / 1e9);
            ultimo[0] = adesso;
            campo.repaint();
        }).start();
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PongApp app = new PongApp();
            app.finestra().setVisible(true);
            app.campo.requestFocusInWindow();
        });
    }
}
