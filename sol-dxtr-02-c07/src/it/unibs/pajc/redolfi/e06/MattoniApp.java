package it.unibs.pajc.redolfi.e06;

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
 * Problema 6, Mattoni (un Breakout).
 *
 * Idea: il modello Mattoni a passo fisso con i sottopassi (i mattoni sono sottili), i
 * mattoni come ostacoli con colpi residui tolti A FINE PASSO; punteggio e vite come
 * proprieta' mostrate da una vista a parte; i livelli da file con FabbricaLivelli; gli
 * stati Menu, InCorso, Pausa, LivelloFinito, Finita; l'audio passato come Audio (qui
 * MUTO) e un listener che lo usa; la racchetta come comando continuo.
 *
 * Errore tipico: togliere il mattone dentro il ciclo degli ostacoli
 * (ConcurrentModificationException o un mattone saltato), o un passo senza sottopassi
 * con cui la pallina attraversa una fila intera.
 */
public class MattoniApp {

    interface Stato {
        default void entra(MattoniApp app) { }
        default void stepNext(MattoniApp app, double dt) { }
        default void tasto(MattoniApp app, int c) { }
        String messaggio();
    }

    static class Menu implements Stato {
        public void tasto(MattoniApp app, int c) { if (c == KeyEvent.VK_SPACE) app.cambia(new InCorso()); }
        public String messaggio() { return "Mattoni  -  frecce e spazio"; }
    }

    static class InCorso implements Stato {
        public void stepNext(MattoniApp app, double dt) {
            app.gioco.muovi((app.premuti.contains(KeyEvent.VK_RIGHT) ? 1 : 0) - (app.premuti.contains(KeyEvent.VK_LEFT) ? 1 : 0));
            app.gioco.stepNext(dt);
            if (app.gioco.vite() <= 0)
                app.cambia(new Finita());
            else if (app.livelloFinito)
                app.cambia(new LivelloFinito());
        }
        public void tasto(MattoniApp app, int c) {
            if (c == KeyEvent.VK_SPACE) app.gioco.lancia();
            if (c == KeyEvent.VK_P) app.cambia(new Pausa());
        }
        public String messaggio() { return null; }
    }

    static class Pausa implements Stato {
        public void tasto(MattoniApp app, int c) { if (c == KeyEvent.VK_P) app.cambia(new InCorso()); }
        public String messaggio() { return "pausa (P)"; }
    }

    static class LivelloFinito implements Stato {
        public void entra(MattoniApp app) {
            app.livelloFinito = false;
            app.livello++;
        }
        public void tasto(MattoniApp app, int c) {
            if (c == KeyEvent.VK_SPACE) {
                if (app.livello > FabbricaLivelli.QUANTI) { app.cambia(new Finita()); return; }
                app.gioco.caricaLivello(FabbricaLivelli.daRisorsa(app.livello));
                app.cambia(new InCorso());
            }
        }
        public String messaggio() { return "livello finito  -  spazio per il prossimo"; }
    }

    static class Finita implements Stato {
        public String messaggio() { return "fine: " + " punti"; }
    }

    final Mattoni gioco = new Mattoni();
    final Set<Integer> premuti = new HashSet<>();
    Stato stato;
    int livello = 1;
    boolean livelloFinito;
    final JLabel testa = new JLabel("punti 0   vite 3", SwingConstants.CENTER);
    final JPanel campo = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double s = Math.min(getWidth() / Mattoni.LARGHEZZA, getHeight() / Mattoni.ALTEZZA);
            for (Mattone m : gioco.mattoni()) {
                Ostacolo o = m.forma();
                g2.setColor(switch (m.tipo()) {
                    case VETRO -> new Color(0x60C0FF);
                    case PIETRA -> m.colpiResidui() == 3 ? new Color(0xC2702A) : m.colpiResidui() == 2 ? new Color(0xD08A50) : new Color(0xE0A880);
                    case ACCIAIO -> new Color(0x6A7080);
                });
                g2.fill(new Rectangle2D.Double(o.x() * s, getHeight() - (o.y() + o.altezza()) * s, o.larghezza() * s, o.altezza() * s));
            }
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Double((gioco.racchettaX() - Mattoni.RACCHETTA_W / 2) * s, getHeight() - Mattoni.RACCHETTA_H * s,
                    Mattoni.RACCHETTA_W * s, Mattoni.RACCHETTA_H * s));
            double r = Mattoni.RAGGIO * s;
            g2.fill(new Ellipse2D.Double(gioco.pallina().x() * s - r, getHeight() - gioco.pallina().y() * s - r, 2 * r, 2 * r));
            if (stato.messaggio() != null) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
                String m = stato instanceof Finita ? "fine: " + gioco.punteggio() + " punti" : stato.messaggio();
                g2.drawString(m, (getWidth() - g2.getFontMetrics().stringWidth(m)) / 2, getHeight() / 2);
            }
        }
    };

    void cambia(Stato nuovo) {
        stato = nuovo;
        nuovo.entra(this);
        campo.repaint();
    }

    JFrame finestra(Audio audio) {
        gioco.caricaLivello(FabbricaLivelli.daRisorsa(1));
        gioco.addPropertyChangeListener(e -> testa.setText("punti " + gioco.punteggio() + "   vite " + gioco.vite()));
        gioco.addListener(new Mattoni.Listener() {      // i suoni: un listener con l'Audio passato
            @Override public void rimbalzo()                      { audio.toc(0.5); }
            @Override public void mattoneRotto(Mattone m, int p)  { audio.pop(); }
            @Override public void pallinaPersa()                  { audio.toc(1); }
            @Override public void livelloFinito()                 { livelloFinito = true; }
        });
        testa.setFont(testa.getFont().deriveFont(Font.BOLD, 22f));
        campo.setBackground(new Color(0x101820));
        campo.setFocusable(true);
        campo.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e)  { premuti.add(e.getKeyCode()); stato.tasto(MattoniApp.this, e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { premuti.remove(e.getKeyCode()); }
        });
        cambia(new Menu());
        JFrame f = new JFrame("Mattoni");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(testa, BorderLayout.NORTH);
        f.add(campo, BorderLayout.CENTER);
        f.setSize(720, 600);
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
            MattoniApp app = new MattoniApp();
            app.finestra(Audio.MUTO).setVisible(true);
            app.campo.requestFocusInWindow();
        });
    }
}
