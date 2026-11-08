package it.unibs.pajc.redolfi.e01;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 3.1, il termostato.
 *
 * Idea: il modello ha tre proprieta' annunciate con PropertyChangeSupport; lo spinner e'
 * legato alla desiderata nei due versi (il giro si chiude perche' firePropertyChange tace
 * quando il valore non cambia); il termometro si ridisegna su evento; la console ascolta
 * solo "riscaldamentoAcceso". ProvaTermostato fa passare un minuto senza finestra.
 *
 * Errore tipico: non annunciare la proprieta' derivata riscaldamentoAcceso, che non ha un
 * setter: la console non saprebbe mai quando il riscaldamento parte. Si annuncia in ogni
 * punto in cui puo' cambiare, confrontando il valore prima e dopo.
 */
public class TermostatoApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Termostato t = new Termostato();
            t.addPropertyChangeListener(e -> {               // la console
                if (e.getPropertyName().equals("riscaldamentoAcceso"))
                    System.out.printf("t = %5.1f s  riscaldamento %s%n", t.tempo(),
                            (Boolean) e.getNewValue() ? "ACCESO" : "spento");
            });

            JSpinner spinner = new JSpinner(new SpinnerNumberModel(t.desiderata(), 10.0, 30.0, 0.5));
            spinner.addChangeListener(e -> t.setDesiderata((Double) spinner.getValue()));
            t.addPropertyChangeListener(e -> {
                if (e.getPropertyName().equals("desiderata"))
                    spinner.setValue(e.getNewValue());
            });

            JPanel barra = new JPanel(new FlowLayout());
            barra.add(new JLabel("Desiderata"));
            barra.add(spinner);
            JFrame f = new JFrame("Termostato");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(barra, BorderLayout.NORTH);
            f.add(new PnlTermometro(t), BorderLayout.CENTER);
            f.setSize(320, 420);
            f.setVisible(true);

            long[] ultimo = { System.nanoTime() };
            new Timer(50, e -> {
                long adesso = System.nanoTime();
                t.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
            }).start();
        });
    }
}
