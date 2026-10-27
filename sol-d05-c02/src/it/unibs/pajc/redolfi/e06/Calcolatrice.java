package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Esercizio 2.6 - la finestra di una calcolatrice, solo la composizione.
 *
 * Il content pane ha un BorderLayout: il display va a NORTH, dove riceve tutta la larghezza
 * e la sua altezza preferita (quindi si allarga ma non si alza); la tastiera va al CENTER,
 * che prende tutto lo spazio che avanza. La tastiera e' un GridLayout(4, 4): celle tutte
 * uguali, che crescono insieme. Le etichette stanno in un array nell'ordine della griglia e
 * un ciclo crea i pulsanti: il GridLayout li dispone riga per riga nell'ordine di add.
 *
 * Errore tipico: sedici add scritti a mano, con un tasto fuori posto; oppure il display al
 * CENTER e la tastiera a SOUTH, cosi' a crescere e' il display.
 */
public class Calcolatrice extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String[] TASTI = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+" };

    private final JTextField txtDisplay = new JTextField("0");

    public Calcolatrice() {
        super("Calcolatrice");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        txtDisplay.setEditable(false);
        txtDisplay.setHorizontalAlignment(JTextField.RIGHT);
        txtDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));

        JPanel pnlTastiera = new JPanel(new GridLayout(4, 4, 4, 4));
        Font fontTasti = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        for (String t : TASTI) {
            JButton b = new JButton(t);
            b.setFont(fontTasti);
            pnlTastiera.add(b);
        }

        JPanel pnlTutto = new JPanel(new BorderLayout(0, 8));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(txtDisplay, BorderLayout.NORTH);
        pnlTutto.add(pnlTastiera, BorderLayout.CENTER);
        setContentPane(pnlTutto);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calcolatrice().setVisible(true));
    }
}
