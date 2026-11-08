package it.unibs.pajc.redolfi.e02;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 3.2, il cronometro.
 *
 * Idea: il modello annuncia tick (ogni decimo di secondo misurato) e giro; l'etichetta
 * del tempo e la lista dei giri sono listener; i quattro pulsanti e il timer sono il
 * controllore. L'etichetta si aggiorna dieci volte al secondo, non sessanta.
 *
 * Errore tipico: annunciare tick a ogni passo del timer, legando il ritmo degli eventi a
 * quello dello schermo. Il modello accumula i dt e annuncia quando il decimo e' passato.
 */
public class CronometroApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Cronometro c = new Cronometro();

            JLabel tempo = new JLabel("0,0", SwingConstants.CENTER);
            tempo.setFont(tempo.getFont().deriveFont(Font.BOLD, 56f));
            DefaultListModel<String> giri = new DefaultListModel<>();
            c.addCronometroListener(new CronometroListener() {
                @Override
                public void tick(double t) {
                    tempo.setText(String.format("%.1f", t));
                }

                @Override
                public void giro(int n, double tGiro, double tTot) {
                    giri.addElement(String.format("giro %d: %.1f s  (totale %.1f s)", n, tGiro, tTot));
                }
            });

            JPanel comandi = new JPanel(new FlowLayout());
            for (var voce : new Object[][] { { "Avvia", (Runnable) c::avvia }, { "Ferma", (Runnable) c::ferma },
                    { "Giro", (Runnable) c::giro }, { "Azzera", (Runnable) () -> { c.azzera(); giri.clear(); } } }) {
                JButton b = new JButton((String) voce[0]);
                b.addActionListener(e -> ((Runnable) voce[1]).run());
                comandi.add(b);
            }

            JFrame f = new JFrame("Cronometro");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(tempo, BorderLayout.NORTH);
            f.add(new JScrollPane(new JList<>(giri)), BorderLayout.CENTER);
            f.add(comandi, BorderLayout.SOUTH);
            f.setSize(360, 400);
            f.setVisible(true);

            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                c.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
            }).start();
        });
    }
}
