package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 * Esercizio 8.7: le righe arrivano.
 *
 * Al click, un SwingWorker<Void, String> genera diecimila righe con una pausa di un
 * millisecondo ciascuna (simula la lettura di un file lento) e le pubblica con
 * publish(riga); process(List<String>) gira sull'EDT e le aggiunge alla JTextArea.
 * Swing raggruppa le righe pubblicate fra un evento e l'altro: process riceve blocchi
 * di decine di righe, non una alla volta (il programma stampa la dimensione di ogni
 * blocco). Durante il caricamento la finestra resta reattiva: si puo' scorrere, ridurre,
 * spostare. done() riabilita il pulsante.
 *
 * Errore tipico: chiamare textArea.append dentro doInBackground: funziona quasi sempre,
 * ed e' un accesso a un componente dal thread sbagliato. E leggere il file dentro
 * process: process gira sull'EDT, e lo congelerebbe.
 */
public class LeRigheArrivano extends JFrame {
    private final JTextArea area = new JTextArea(20, 60);
    private final JButton btnCarica = new JButton("Carica");

    public LeRigheArrivano() {
        super("Le righe arrivano");
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(btnCarica, BorderLayout.SOUTH);
        btnCarica.addActionListener(e -> carica());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private void carica() {
        btnCarica.setEnabled(false);
        area.setText("");
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws InterruptedException {   // thread di lavoro
                for (int i = 1; i <= 10_000; i++) {
                    Thread.sleep(1);                                       // "lettura lenta"
                    publish("riga " + i + ": " + "x".repeat(i % 40));
                }
                return null;
            }

            @Override
            protected void process(List<String> righe) {                    // sull'EDT
                System.out.println("process: " + righe.size() + " righe in questo blocco");
                for (String r : righe) area.append(r + "\n");
            }

            @Override
            protected void done() {                                         // sull'EDT
                btnCarica.setEnabled(true);
                area.append("-- fine --\n");
            }
        }.execute();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new LeRigheArrivano().setVisible(true));
    }
}
