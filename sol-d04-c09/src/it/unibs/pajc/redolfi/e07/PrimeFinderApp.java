package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * Palestra 9.7: PrimeFinderApp completa. Chiude il problema d'apertura della dispensa.
 *
 * Tre livelli di thread, tutti cooperativi:
 *  - EDT: listener, barra, etichette; legge i campi PRIMA di partire;
 *  - il thread del SwingWorker (doInBackground): crea il pool in un try con risorse,
 *    consegna 64 compiti, e man mano che ogni Future completa (nell'ordine di
 *    consegna) somma e chiama setProgress; se viene interrotto (cancel(true)), il
 *    finally fa shutdownNow() sul pool, che interrompe i compiti in corso;
 *  - i thread del pool: contano, e ogni centomila numeri guardano isInterrupted().
 * done() gira sull'EDT: risultato e tempo, oppure "annullato", e riabilita i pulsanti.
 * Alla chiusura della finestra EXIT_ON_CLOSE termina la JVM; se un conteggio e' in
 * corso, windowClosing lo annulla prima, cosi' nessun thread resta a calcolare.
 *
 * Con 100 000 000 e 16 thread: poco piu' di un secondo, barra che sale, finestra viva.
 */
public class PrimeFinderApp extends JFrame {
    private final JTextField campoMax = new JTextField("100000000", 10);
    private final JTextField campoThread = new JTextField(String.valueOf(Runtime.getRuntime().availableProcessors()), 3);
    private final JButton btnConta = new JButton("Conta");
    private final JButton btnAnnulla = new JButton("Annulla");
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel lblRisultato = new JLabel("-", SwingConstants.CENTER);
    private SwingWorker<Long, Void> worker;

    public PrimeFinderApp() {
        super("Numeri primi");
        JPanel sopra = new JPanel();
        sopra.add(new JLabel("Fino a:"));
        sopra.add(campoMax);
        sopra.add(new JLabel("thread:"));
        sopra.add(campoThread);
        sopra.add(btnConta);
        sopra.add(btnAnnulla);
        add(sopra, BorderLayout.NORTH);
        add(barra, BorderLayout.CENTER);
        add(lblRisultato, BorderLayout.SOUTH);
        btnConta.addActionListener(e -> conta());
        btnAnnulla.addActionListener(e -> { if (worker != null) worker.cancel(true); });
        btnAnnulla.setEnabled(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (worker != null) worker.cancel(true); }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private void conta() {                                               // EDT
        long max = Long.parseLong(campoMax.getText().trim());
        int nThread = Integer.parseInt(campoThread.getText().trim());
        btnConta.setEnabled(false);
        btnAnnulla.setEnabled(true);
        barra.setValue(0);
        lblRisultato.setText("conto...");
        long t0 = System.nanoTime();

        worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {           // thread del worker
                int nTask = 64;
                long perTask = max / nTask, totale = 0;
                ExecutorService pool = Executors.newFixedThreadPool(nThread);
                try {
                    List<Future<Long>> risultati = new ArrayList<>();
                    for (int i = 0; i < nTask; i++) {
                        long da = i * perTask + 1, a = (i == nTask - 1) ? max : (i + 1) * perTask;
                        risultati.add(pool.submit(() -> PrimeTester.count(da, a)));
                    }
                    int fatti = 0;
                    for (Future<Long> f : risultati) {
                        long n = f.get();                                // InterruptedException se annullato
                        if (n < 0) throw new InterruptedException();     // un compito interrotto
                        totale += n;
                        setProgress(100 * ++fatti / nTask);
                    }
                    return totale;
                } finally {
                    pool.shutdownNow();                                  // propaga l'interruzione ai compiti
                }
            }

            @Override
            protected void done() {                                      // EDT
                try {
                    long n = get();
                    lblRisultato.setText(String.format("primi: %,d in %d ms con %d thread", n,
                            (System.nanoTime() - t0) / 1_000_000, nThread));
                } catch (CancellationException e) {
                    lblRisultato.setText("annullato");
                    barra.setValue(0);
                } catch (InterruptedException | ExecutionException e) {
                    lblRisultato.setText("errore: " + e.getCause());
                }
                btnConta.setEnabled(true);
                btnAnnulla.setEnabled(false);
            }
        };
        worker.addPropertyChangeListener(e -> {
            if ("progress".equals(e.getPropertyName())) barra.setValue((Integer) e.getNewValue());
        });
        worker.execute();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new PrimeFinderApp().setVisible(true));
    }
}
