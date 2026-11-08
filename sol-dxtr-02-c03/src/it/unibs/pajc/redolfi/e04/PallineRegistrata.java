package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.4, il registratore: Palline con un listener che scrive ogni evento su
 * palline.log (nella cartella di lavoro). Chiudendo la finestra il registratore si
 * scollega e chiude il file. Poi Riproduzione rilegge il file.
 *
 * Idea: il registratore e' un listener come gli altri (del mondo per rimbalzi e
 * fermate, del pannello per i punti scelti) e ha bisogno del tempo del mondo: Mondo ha
 * guadagnato tempo(), sommato in avanza. Gli eventi sono record immutabili e si possono
 * scrivere con calma.
 *
 * Errore tipico: registrare i rimbalzi con il dt misurato del timer e aspettarsi che la
 * riproduzione a passo fisso li ritrovi agli stessi istanti: la fisica dipende dal passo
 * (capitolo 4). Qui il controllore e' quello della dispensa, a dt misurato, e la
 * riproduzione dice quanti eventi coincidono: non tutti.
 */
public class PallineRegistrata {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                Mondo mondo = new Mondo(8, 6);
                PnlMondo vista = new PnlMondo(mondo);
                JSlider sld = new JSlider(0, 2500);
                JToggleButton pausa = new JToggleButton("Pausa");
                Controllore controllore = new Controllore(mondo, vista, sld, pausa);
                Registratore reg = new Registratore(mondo, vista, "palline.log");

                JFrame f = new JFrame("Palline, registrata in palline.log");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(vista, BorderLayout.CENTER);
                f.add(new PnlStato(mondo), BorderLayout.SOUTH);
                f.setSize(640, 480);
                f.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowOpened(WindowEvent e) {
                        controllore.avvia();
                    }

                    @Override
                    public void windowClosing(WindowEvent e) {
                        reg.close();
                    }
                });
                f.setVisible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
