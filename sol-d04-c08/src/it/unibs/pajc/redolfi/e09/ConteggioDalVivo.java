package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Esercizio 8.9: il conteggio dal vivo.
 *
 * Chi tocca che cosa, e da quale thread:
 *  - EDT: il listener di "Avvia" crea il LongAdder, avvia il Timer, consegna i 64
 *    compiti a un executor proprio e, su un thread di servizio, aspetta la fine;
 *    il listener del Timer (sull'EDT) legge trovati.sum() e aggiorna l'etichetta;
 *    il Runnable di invokeLater scrive "finito" e ferma il Timer.
 *  - thread del pool: contano i primi del proprio intervallo in una variabile locale e
 *    alla fine fanno trovati.add(n): il LongAdder e' l'unico dato condiviso, ed e'
 *    thread-safe (per vedere la barra muoversi, add avviene a blocchi di 100 000).
 *  - thread "attesa": chiude l'executor (close = shutdown + attesa di tutti) e poi
 *    rientra con invokeLater; non tocca nessun componente.
 * Nessun lock scritto a mano: tutto e' confinato (variabili locali, EDT) o thread-safe
 * (LongAdder).
 *
 * Errore tipico: aspettare la fine dell'executor nel listener di "Avvia": l'EDT si
 * bloccherebbe per tutto il conteggio. L'attesa va su un thread suo.
 */
public class ConteggioDalVivo extends JFrame {
    private final JLabel lblConteggio = new JLabel("0", SwingConstants.CENTER);
    private final JButton btnAvvia = new JButton("Avvia");
    private final Timer timer;
    private LongAdder trovati;

    public ConteggioDalVivo() {
        super("Primi trovati finora");
        lblConteggio.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        add(lblConteggio, BorderLayout.CENTER);
        add(btnAvvia, BorderLayout.SOUTH);
        timer = new Timer(1000, e -> lblConteggio.setText(String.format("%,d", trovati.sum())));   // EDT
        btnAvvia.addActionListener(e -> avvia());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 180);
    }

    private void avvia() {                                              // EDT
        btnAvvia.setEnabled(false);
        trovati = new LongAdder();
        timer.start();
        long max = 100_000_000L;
        int nTask = 64, nThread = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(nThread);
        long perTask = max / nTask;
        for (int i = 0; i < nTask; i++) {
            long da = i * perTask + 1, a = (i == nTask - 1) ? max : (i + 1) * perTask;
            executor.execute(() -> {                                    // thread del pool
                long n = 0;
                for (long k = da; k <= a; k++) {
                    if (PrimeTester.isPrime(k)) n++;
                    if (k % 100_000 == 0) { trovati.add(n); n = 0; }     // a blocchi, per vederlo salire
                }
                trovati.add(n);
            });
        }
        new Thread(() -> {                                              // thread "attesa"
            executor.close();                                           // aspetta tutti i compiti
            EventQueue.invokeLater(() -> {                              // rientro sull'EDT
                timer.stop();
                lblConteggio.setText(String.format("%,d  finito", trovati.sum()));
                btnAvvia.setEnabled(true);
            });
        }, "attesa").start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ConteggioDalVivo().setVisible(true));
    }
}
