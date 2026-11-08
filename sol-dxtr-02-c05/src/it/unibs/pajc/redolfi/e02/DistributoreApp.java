package it.unibs.pajc.redolfi.e02;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** La vista Swing del distributore: pulsanti che creano comandi, etichette che ascoltano. */
public class DistributoreApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Distributore d = new Distributore();
            JLabel display = new JLabel("inserire monete", SwingConstants.CENTER);
            display.setFont(display.getFont().deriveFont(Font.BOLD, 18f));
            JLabel credito = new JLabel("credito 0", SwingConstants.CENTER);
            JLabel stato = new JLabel("in attesa", SwingConstants.CENTER);
            d.addDistributoreListener(new DistributoreListener() {
                @Override public void creditoCambiato(int c)    { credito.setText("credito " + c + " cent"); }
                @Override public void prodottoErogato(String p) { display.setText("ecco " + p); }
                @Override public void restoDato(int c)          { display.setText("resto " + c + " cent"); }
                @Override public void messaggio(String t)       { display.setText(t); }
                @Override public void statoCambiato(String n)   { stato.setText(n); }
            });
            JPanel pulsanti = new JPanel(new GridLayout(0, 3, 4, 4));
            Object[][] voci = { { "10 cent", new Moneta(10) }, { "50 cent", new Moneta(50) }, { "1 euro", new Moneta(100) },
                    { "acqua", new Scelta("acqua") }, { "caffe", new Scelta("caffe") }, { "succo", new Scelta("succo") },
                    { "resto", new Resto() }, { "guasto", new Guasto() }, { "riparato", new Riparato() } };
            for (Object[] v : voci) {
                JButton b = new JButton((String) v[0]);
                b.addActionListener(e -> d.esegui((Comando) v[1]));
                pulsanti.add(b);
            }
            JPanel alto = new JPanel(new GridLayout(3, 1));
            alto.add(display);
            alto.add(credito);
            alto.add(stato);
            JFrame f = new JFrame("Distributore");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(alto, BorderLayout.NORTH);
            f.add(pulsanti, BorderLayout.CENTER);
            f.setSize(360, 300);
            f.setVisible(true);
            long[] ultimo = { System.nanoTime() };
            new Timer(50, e -> {
                long adesso = System.nanoTime();
                d.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
            }).start();
        });
    }
}
