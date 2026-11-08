package it.unibs.pajc.redolfi.e04;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Esercizio 2.4, una seconda vista, a parole.
 *
 * Idea: VistaTestuale e' una vista come PnlMondo, solo che scrive a console invece di
 * disegnare; non e' un componente Swing e non ha bisogno di esserlo. Il controllore
 * conosce entrambe e le aggiorna a ritmi diversi: il pannello a ogni passo, la console
 * quando i dt accumulati superano un secondo. Mondo e PnlMondo sono quelli della dispensa,
 * senza una riga cambiata: il modello non sa quante viste lo guardano.
 *
 * Errore tipico: contare i passi del timer ("ogni 60 passi") invece di sommare i dt
 * misurati: su un computer carico la riga arriverebbe piu' di rado, e il ritmo
 * dipenderebbe dallo schermo e non dal tempo.
 */
public class PallineApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mondo mondo = new Mondo(8, 6);
            PnlMondo vista = new PnlMondo(mondo);
            Controllore controllore = new Controllore(mondo, vista, new VistaTestuale());

            JFrame frame = new JFrame("Palline, con la console");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(vista);
            frame.setSize(640, 480);
            frame.setVisible(true);
            controllore.avvia();
        });
    }
}
