package it.unibs.pajc.redolfi.e02;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 2.2, il semaforo pedonale.
 *
 * Idea: il modello Semaforo conta i secondi che il controllore gli passa con stepNext(dt),
 * misurati con nanoTime; la vista disegna le luci leggendo il modello; il pulsante chiama
 * semaforo.chiamata(). Il ciclo delle fasi e la regola della chiamata stanno tutti nel
 * modello, e ProvaSemaforo li verifica facendo passare 60 secondi a passi di un decimo,
 * senza finestra.
 *
 * Errore tipico: cambiare luce "ogni n scatti del timer". Il ciclo durerebbe di piu' sui
 * computer lenti (il timer e' una richiesta, non una garanzia) e la prova senza finestra
 * sarebbe impossibile, perche' il tempo entrerebbe nel modello da Swing.
 */
public class SemaforoApp {

    private final Semaforo semaforo = new Semaforo();
    private final PnlSemaforo vista = new PnlSemaforo(semaforo);
    private final Timer timer = new Timer(50, e -> stepNext());
    private long ultimo;

    private void stepNext() {
        long adesso = System.nanoTime();
        semaforo.stepNext((adesso - ultimo) / 1e9);
        ultimo = adesso;
        vista.repaint();
    }

    private JFrame finestra() {
        JButton chiamata = new JButton("Chiamata pedonale");
        chiamata.addActionListener(e -> semaforo.chiamata());
        JFrame f = new JFrame("Semaforo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(vista, BorderLayout.CENTER);
        f.add(chiamata, BorderLayout.SOUTH);
        f.setSize(360, 420);
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SemaforoApp app = new SemaforoApp();
            app.finestra().setVisible(true);
            app.ultimo = System.nanoTime();
            app.timer.start();
        });
    }
}
