package it.unibs.pajc.redolfi.e05;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** La finestra del biliardo con le buche: il tavolo, il punteggio come vista a parte, Rivedi. */
public class BiliardoApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Partita partita = new Partita();
            PnlTavolo tavolo = new PnlTavolo(partita);
            JLabel punteggio = new JLabel("0  :  0"), turno = new JLabel();
            punteggio.setFont(punteggio.getFont().deriveFont(Font.BOLD, 28f));
            partita.addListener(new Partita.Listener() {
                @Override public void punteggio(int a, int b) { punteggio.setText(a + "  :  " + b); }
                @Override public void turno(int g)            { turno.setText("   " + partita.stato().nome()); }
                @Override public void finita(int v)           { turno.setText("   fine: vince il giocatore " + v); }
            });
            JButton rivedi = new JButton("Rivedi l'ultimo colpo");
            rivedi.addActionListener(e -> partita.rivedi());
            JPanel alto = new JPanel(new FlowLayout(FlowLayout.LEFT));
            alto.add(punteggio);
            alto.add(turno);
            alto.add(rivedi);
            JFrame f = new JFrame("Biliardo con le buche");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(alto, BorderLayout.NORTH);
            f.add(tavolo, BorderLayout.CENTER);
            f.setSize(900, 540);
            f.setVisible(true);
            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                partita.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
                tavolo.repaint();
            }).start();
        });
    }
}
