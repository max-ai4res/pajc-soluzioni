package it.unibs.pajc.redolfi.e02;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Problema 2, il contatore in tre versioni.
 *
 * Idea: lo stesso Contatore guardato da tre finestre. La prima e' aggiornata dal
 * controllore a ogni passo del timer (polling: ~60 setText al secondo, quasi tutti
 * inutili); la seconda ascolta un ContatoreListener scritto a mano; la terza la
 * proprieta' "valore". Le ultime due fanno un setText al secondo. Ogni vista conta i
 * propri setText e li mostra. ProvaContatore verifica valore e annunci senza finestra.
 *
 * Errore tipico: tre contatori invece di uno, o un modello che annuncia a ogni passo
 * anche se il valore non cambia.
 */
public class TreVersioni {

    /** Un'etichetta che conta quante volte le viene chiesto di cambiare testo. */
    static class Vista extends JLabel {
        int chiamate;

        Vista() {
            super("0", SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 28f));
        }

        void mostra(int valore) {
            chiamate++;
            setText(valore + "   (setText: " + chiamate + ")");
        }
    }

    static JFrame finestra(String titolo, Vista v, int x, Contatore c) {
        JButton azzera = new JButton("Azzera");
        azzera.addActionListener(e -> c.azzera());
        JFrame f = new JFrame(titolo);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(v, BorderLayout.CENTER);
        f.add(azzera, BorderLayout.SOUTH);
        f.setSize(360, 160);
        f.setLocation(x, 100);
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Contatore c = new Contatore();                     // uno solo
            Vista polling = new Vista(), aMano = new Vista(), proprieta = new Vista();
            c.addContatoreListener(aMano::mostra);
            c.addPropertyChangeListener(e -> proprieta.mostra((Integer) e.getNewValue()));
            finestra("1. polling", polling, 50, c).setVisible(true);
            finestra("2. listener a mano", aMano, 430, c).setVisible(true);
            finestra("3. PropertyChangeSupport", proprieta, 810, c).setVisible(true);
            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                c.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
                polling.mostra(c.valore());                    // il controllore aggiorna la prima a ogni passo
            }).start();
        });
    }
}
