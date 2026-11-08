package it.unibs.pajc.redolfi.e04;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 4.4, il flipper minimo.
 *
 * Idea: il motore del capitolo per una pallina sola, piu' due novita' nel modello: i
 * respingenti con restituzione 1,3 (superfici attive) e le alette, segmenti che ruotano
 * attorno a un perno; nel contatto con l'aletta entra la velocita' del punto toccato
 * (omega x r) come velocita' relativa, con massa infinita. Un tetto alla velocita' tiene a
 * bada l'energia che i respingenti regalano. Il punteggio e' annunciato come evento.
 *
 * Errore tipico: dimenticare il tetto. Fra due respingenti vicini la pallina guadagna
 * energia a ogni colpo e in pochi secondi attraversa tutto.
 */
public class FlipperApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Flipper flipper = new Flipper();
            PnlFlipper vista = new PnlFlipper(flipper);
            JFrame f = new JFrame("Flipper");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(vista);
            f.setSize(420, 720);
            f.setVisible(true);
            vista.requestFocusInWindow();
            vista.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    tasto(e, true);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    tasto(e, false);
                }

                void tasto(KeyEvent e, boolean giu) {
                    if (e.getKeyCode() == KeyEvent.VK_Z)
                        flipper.premi(0, giu);
                    if (e.getKeyCode() == KeyEvent.VK_M)
                        flipper.premi(1, giu);
                }
            });
            flipper.addPunteggioListener(p -> f.setTitle("Flipper - " + p + " punti"));
            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                flipper.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
                vista.repaint();
            }).start();
        });
    }
}
