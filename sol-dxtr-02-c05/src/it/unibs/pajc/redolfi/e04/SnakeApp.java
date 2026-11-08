package it.unibs.pajc.redolfi.e04;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 5.4, Snake.
 *
 * Idea: il modello Serpente avanza di una cella ogni T secondi di tempo ricevuto (T cala a
 * ogni mela); le frecce sono comandi gira() messi in una piccola coda e consumati un passo
 * alla volta (due frecce fra un passo e l'altro valgono entrambe, in ordine, e una
 * direzione opposta e' ignorata). Gli stati Menu, InCorso, Pausa, Finita sono classi con
 * i default "niente", e il controllore (questa classe) ha un solo cambia(). ProvaSerpente
 * guida il serpente verso la mela e verso il muro senza finestra.
 *
 * Errore tipico: applicare la direzione subito alla pressione: "su" e poi "sinistra"
 * nello stesso intervallo fanno girare il serpente di 180 gradi dentro il proprio corpo.
 */
public class SnakeApp {

    private Serpente serpente = new Serpente(20, 28, System.nanoTime());
    private Stato stato;
    private final JPanel vista = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int l = Math.min(getWidth() / serpente.colonne(), getHeight() / serpente.righe());
            g2.setColor(new Color(0xC94F2E));
            g2.fillOval(serpente.mela().colonna() * l + 2, serpente.mela().riga() * l + 2, l - 4, l - 4);
            g2.setColor(new Color(0x2E7D8C));
            for (Cella c : serpente.corpo())
                g2.fillRoundRect(c.colonna() * l + 1, c.riga() * l + 1, l - 2, l - 2, 6, 6);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("punti " + serpente.punti(), 8, getHeight() - 8);
            if (stato.messaggio() != null) {
                g2.setColor(new Color(255, 255, 255, 170));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.BLACK);
                g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
                int w = g2.getFontMetrics().stringWidth(stato.messaggio());
                g2.drawString(stato.messaggio(), (getWidth() - w) / 2, getHeight() / 2);
            }
        }
    };

    public Serpente serpente() { return serpente; }

    void nuovaPartita() {
        serpente = new Serpente(20, 28, System.nanoTime());
    }

    /** L'unico posto in cui si cambia stato. */
    void cambia(Stato nuovo) {
        stato = nuovo;
        nuovo.entra(this);
        vista.repaint();
    }

    // ---- gli stati

    static class Menu implements Stato {
        public void tasto(SnakeApp app, int codice) {
            if (codice == KeyEvent.VK_SPACE)
                app.cambia(new InCorso());
        }

        public String messaggio() { return "Snake  -  spazio per cominciare"; }
    }

    static class InCorso implements Stato {
        public void stepNext(SnakeApp app, double dt) {
            app.serpente.stepNext(dt);
            if (app.serpente.finita())
                app.cambia(new Finita());
        }

        public void tasto(SnakeApp app, int codice) {
            switch (codice) {
                case KeyEvent.VK_UP -> app.serpente.gira(Direzione.SU);
                case KeyEvent.VK_DOWN -> app.serpente.gira(Direzione.GIU);
                case KeyEvent.VK_LEFT -> app.serpente.gira(Direzione.SINISTRA);
                case KeyEvent.VK_RIGHT -> app.serpente.gira(Direzione.DESTRA);
                case KeyEvent.VK_SPACE -> app.cambia(new Pausa());
                default -> { }
            }
        }

        public String messaggio() { return null; }
    }

    static class Pausa implements Stato {
        public void tasto(SnakeApp app, int codice) {
            if (codice == KeyEvent.VK_SPACE)
                app.cambia(new InCorso());
        }

        public String messaggio() { return "pausa"; }
    }

    static class Finita implements Stato {
        private int punti;

        public void entra(SnakeApp app) {
            punti = app.serpente.punti();
        }

        public void tasto(SnakeApp app, int codice) {
            if (codice == KeyEvent.VK_SPACE) {
                app.nuovaPartita();
                app.cambia(new InCorso());
            }
        }

        public String messaggio() { return "fine: " + punti + " punti  -  spazio per ricominciare"; }
    }

    // ---- la finestra

    private JFrame finestra() {
        cambia(new Menu());
        vista.setBackground(new Color(0xF4F6FA));
        vista.setFocusable(true);
        vista.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                stato.tasto(SnakeApp.this, e.getKeyCode());
            }
        });
        JFrame f = new JFrame("Snake");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(vista);
        f.setSize(28 * 26 + 16, 20 * 26 + 60);
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
            SnakeApp app = new SnakeApp();
            JFrame f = app.finestra();
            f.setVisible(true);
            app.vista.requestFocusInWindow();
        });
    }
}
