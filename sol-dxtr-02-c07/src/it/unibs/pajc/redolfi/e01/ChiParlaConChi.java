package it.unibs.pajc.redolfi.e01;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Problema 1, chi parla con chi.
 *
 * Le tre versioni, giudicate con la tabella del capitolo 2:
 *  1. la vista tiene il numero e lo incrementa nel listener: viola "la vista non tiene i
 *     dati" e "la vista non decide che cosa significa un click"; non c'e' un modello, quindi
 *     niente test senza finestra; una seconda etichetta vuole una seconda copia del numero;
 *  2. il modello importa JLabel: viola "il modello non importa Swing" e "non conosce la
 *     vista"; il test apre un'etichetta; una seconda etichetta vuole un secondo parametro
 *     nel costruttore del modello;
 *  3. il controllore chiama il modello, il modello annuncia, l'etichetta ascolta: tutte le
 *     regole rispettate; una seconda etichetta e' un altro listener, il test non apre
 *     nulla.
 *
 * La sequenza della terza versione (verificata con le stampe):
 *  1. il thread degli eventi chiama actionPerformed del listener del pulsante (controllore)
 *  2. il controllore chiama contatore.incrementa()
 *  3. incrementa cambia il campo
 *  4. incrementa chiama firePropertyChange
 *  5. PropertyChangeSupport chiama propertyChange dell'etichetta (ancora dentro incrementa)
 *  6. l'etichetta chiama setText
 *  7. si ritorna: da propertyChange, da fire, da incrementa, da actionPerformed
 *  8. Swing ridisegna l'etichetta, piu' tardi, quando la coda e' vuota.
 *
 * Errore tipico: scrivere la sequenza "a senso" con setText prima dell'annuncio, o con
 * l'annuncio fatto dal controllore: nella terza versione il controllore non sa che
 * esiste un'etichetta.
 */
public class ChiParlaConChi {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Contatore contatore = new Contatore();
            JLabel etichetta = new JLabel("0", SwingConstants.CENTER);
            etichetta.setFont(etichetta.getFont().deriveFont(Font.BOLD, 40f));
            contatore.addPropertyChangeListener(e -> {        // la vista ascolta
                System.out.println("  5. l'etichetta riceve propertyChange(" + e.getNewValue() + ")");
                System.out.println("  6. l'etichetta chiama setText");
                etichetta.setText(String.valueOf(e.getNewValue()));
            });
            JButton piu = new JButton("+1");
            piu.addActionListener(e -> {                       // il controllore
                System.out.println("  1. actionPerformed del pulsante");
                System.out.println("  2. il controllore chiama contatore.incrementa()");
                contatore.incrementa();
                System.out.println("  7. ritorno: incrementa e actionPerformed finiscono; 8. Swing ridisegnera' l'etichetta");
            });
            JFrame f = new JFrame("Chi parla con chi: guarda la console");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(etichetta, BorderLayout.CENTER);
            f.add(piu, BorderLayout.SOUTH);
            f.setSize(300, 200);
            f.setVisible(true);
        });
    }
}
