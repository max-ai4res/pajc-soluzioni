package it.unibs.pajc.redolfi.e06;

import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Esercizio 4.6 - indovina il numero, solo con finestre di dialogo.
 *
 * Ogni show... e' modale e restituisce la risposta, quindi il gioco si scrive come un
 * normale ciclo, dall'alto in basso. Annullare restituisce null (input) o un valore diverso
 * da YES_OPTION (conferma): in entrambi i casi si esce. Il proprietario e' null perche' non
 * c'e' una finestra principale. Il gioco parte comunque sul thread degli eventi.
 *
 * Errore tipico: Integer.parseInt sulla risposta senza controllare null e senza catturare
 * NumberFormatException: un "annulla" o un "cinquanta" fanno terminare il programma con
 * un'eccezione.
 */
public class IndovinaNumero {

    private static final Random CASO = new Random();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IndovinaNumero::gioca);
    }

    static void gioca() {
        do {
            if (!partita())
                return;                                  // annullato durante la partita
        } while (JOptionPane.showConfirmDialog(null, "Un'altra partita?", "Indovina il numero",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }

    /** Una partita; false se l'utente annulla. */
    static boolean partita() {
        int segreto = 1 + CASO.nextInt(100);
        int tentativi = 0;
        while (true) {
            String s = JOptionPane.showInputDialog(null, "Un numero da 1 a 100:",
                    "Indovina il numero", JOptionPane.QUESTION_MESSAGE);
            if (s == null)
                return false;
            int n;
            try {
                n = Integer.parseInt(s.strip());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "\"" + s + "\" non è un numero.",
                        "Indovina il numero", JOptionPane.ERROR_MESSAGE);
                continue;                                // non conta come tentativo
            }
            tentativi++;
            if (n == segreto) {
                JOptionPane.showMessageDialog(null, "Indovinato in " + tentativi
                        + (tentativi == 1 ? " tentativo!" : " tentativi."));
                return true;
            }
            JOptionPane.showMessageDialog(null, n < segreto ? "Troppo basso." : "Troppo alto.",
                    "Indovina il numero", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
