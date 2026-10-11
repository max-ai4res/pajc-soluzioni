package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Esercizio 8.6: un orologio, in due versioni selezionate dall'argomento.
 *
 *  - "swing" (default): javax.swing.Timer ogni 100 ms. Il listener gira sull'EDT (lo
 *    stampa alla prima esecuzione: AWT-EventQueue-0) e tocca l'etichetta direttamente.
 *  - "executor": ScheduledExecutorService con scheduleAtFixedRate; il compito gira su
 *    pool-1-thread-1 e deve rientrare con invokeLater per toccare l'etichetta.
 *
 * Si preferisce javax.swing.Timer: il compito e' breve e tocca solo l'interfaccia, e il
 * rientro lo fa la libreria. L'executor serve quando il lavoro periodico e' vero lavoro
 * (rete, calcolo) e non deve stare sull'EDT.
 *
 * Errore tipico: aggiornare l'etichetta direttamente dal compito dell'executor:
 * "funziona", ma e' un accesso a un componente dal thread sbagliato. E dimenticare di
 * fermare l'executor alla chiusura: la JVM resterebbe viva a finestra chiusa (qui lo
 * ferma windowClosing, oltre a EXIT_ON_CLOSE).
 */
public class Orologio extends JFrame {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss.S");
    private final JLabel lblOra = new JLabel("--:--:--.-", SwingConstants.CENTER);
    private final JButton btnFerma = new JButton("Ferma"), btnRiprendi = new JButton("Riprendi");
    private boolean primaVolta = true;

    // versione swing
    private Timer timer;
    // versione executor
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> periodico;

    public Orologio(boolean conExecutor) {
        super("Orologio (" + (conExecutor ? "executor + invokeLater" : "javax.swing.Timer") + ")");
        lblOra.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
        add(lblOra, BorderLayout.CENTER);
        JPanel pulsanti = new JPanel();
        pulsanti.add(btnFerma);
        pulsanti.add(btnRiprendi);
        add(pulsanti, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        if (conExecutor) {
            executor = Executors.newScheduledThreadPool(1);
            btnFerma.addActionListener(e -> { if (periodico != null) periodico.cancel(false); });
            btnRiprendi.addActionListener(e -> avviaExecutor());
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { executor.shutdownNow(); }
            });
            avviaExecutor();
        } else {
            timer = new Timer(100, e -> aggiorna());            // il listener gira sull'EDT
            btnFerma.addActionListener(e -> timer.stop());
            btnRiprendi.addActionListener(e -> timer.start());
            timer.start();
        }
    }

    private void avviaExecutor() {
        if (periodico != null && !periodico.isDone()) return;
        periodico = executor.scheduleAtFixedRate(
                () -> EventQueue.invokeLater(this::aggiorna),  // rientro: il compito non tocca componenti
                0, 100, TimeUnit.MILLISECONDS);
    }

    private void aggiorna() {
        if (primaVolta) {
            System.out.println("aggiorna() gira su: " + Thread.currentThread().getName());
            primaVolta = false;
        }
        lblOra.setText(LocalTime.now().format(FORMATO));
    }

    public static void main(String[] args) {
        boolean conExecutor = args.length > 0 && args[0].equals("executor");
        EventQueue.invokeLater(() -> new Orologio(conExecutor).setVisible(true));
    }
}
