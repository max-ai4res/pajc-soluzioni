package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * Esercizio 8.8: Annulla.
 *
 * Rispetto alla terza versione della dispensa cambiano tre cose. Il worker sta in un
 * campo della finestra, perche' il listener di "Annulla" deve raggiungerlo. In
 * doInBackground il ciclo controlla isCancelled() a ogni blocco: cancel(true) interrompe
 * il thread, ma e' il compito che deve accorgersene. In done(), get() lancia
 * CancellationException: e' li', sull'EDT, che si rimette a posto l'interfaccia
 * ("annullato", barra a zero, "Conta" attivo), non nel listener di "Annulla", perche'
 * done() viene chiamato anche dopo l'annullamento e sa quando il compito e' davvero
 * finito.
 *
 * Se il calcolo fosse un blocco unico (count(1, max)), cancel(true) non lo fermerebbe:
 * isPrime non guarda la bandiera di interruzione, e il thread continuerebbe fino in
 * fondo; done() verrebbe comunque chiamato subito (il Future e' annullato), con
 * l'etichetta "annullato" mentre il calcolo gira ancora in sottofondo. Bisognerebbe
 * controllare Thread.currentThread().isInterrupted() dentro il ciclo di count.
 */
public class PrimeFinderAnnulla extends JFrame {
    private final JTextField campoMax = new JTextField("100000000", 10);
    private final JButton btnConta = new JButton("Conta");
    private final JButton btnAnnulla = new JButton("Annulla");
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel lblRisultato = new JLabel("-", SwingConstants.CENTER);
    private SwingWorker<Long, Void> worker;                         // raggiungibile da "Annulla"

    public PrimeFinderAnnulla() {
        super("Numeri primi");
        JPanel sopra = new JPanel();
        sopra.add(new JLabel("Fino a:"));
        sopra.add(campoMax);
        sopra.add(btnConta);
        sopra.add(btnAnnulla);
        add(sopra, BorderLayout.NORTH);
        add(barra, BorderLayout.CENTER);
        add(lblRisultato, BorderLayout.SOUTH);
        btnConta.addActionListener(e -> conta());
        btnAnnulla.addActionListener(e -> { if (worker != null) worker.cancel(true); });
        btnAnnulla.setEnabled(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private void conta() {
        long max = Long.parseLong(campoMax.getText());             // letto sull'EDT, prima
        btnConta.setEnabled(false);
        btnAnnulla.setEnabled(true);
        lblRisultato.setText("conto...");
        worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() {
                int nBlocchi = 100;
                long perBlocco = max / nBlocchi, totale = 0;
                for (int b = 0; b < nBlocchi && !isCancelled(); b++) {
                    totale += PrimeTester.count(b * perBlocco + 1, (b + 1) * perBlocco);
                    setProgress(b + 1);
                }
                return totale;
            }

            @Override
            protected void done() {                                  // sull'EDT, anche dopo cancel
                try {
                    lblRisultato.setText("primi: " + get());
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
            if ("progress".equals(e.getPropertyName()))
                barra.setValue((Integer) e.getNewValue());
        });
        worker.execute();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new PrimeFinderAnnulla().setVisible(true));
    }
}
