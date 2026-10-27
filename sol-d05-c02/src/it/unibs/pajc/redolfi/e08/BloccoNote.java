package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Esercizio 2.8 - la finestra di un blocco note, solo la composizione.
 *
 * L'albero: il content pane (BorderLayout) ha l'elenco a WEST, l'editor al CENTER e la riga
 * di stato a SOUTH. L'elenco e' a sua volta un BorderLayout: la lista nello scroll pane al
 * CENTER (cresce in altezza), i due pulsanti in un GridLayout(1, 2) a SOUTH (stessa
 * larghezza, uno accanto all'altro). L'editor e' un BorderLayout con la riga del titolo a
 * NORTH (etichetta a WEST, campo al CENTER) e il testo nello scroll pane al CENTER.
 * Ingrandendo, il CENTER del content pane, cioe' l'editor, prende lo spazio in larghezza;
 * la colonna dell'elenco cresce solo in altezza.
 *
 * La larghezza dell'elenco la fissa setPrototypeCellValue: senza, la colonna sarebbe larga
 * quanto il titolo piu' lungo, e cambierebbe a ogni nota.
 */
public class BloccoNote extends JFrame {

    private static final long serialVersionUID = 1L;

    public BloccoNote() {
        super("Blocco note");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // --- ovest: l'elenco con i pulsanti sotto
        JList<String> lstNote = new JList<>(new String[] {
                "Spesa", "Idee per il progetto", "Libri da leggere", "Esame di PAJC" });
        lstNote.setPrototypeCellValue("Un titolo abbastanza lungo");
        lstNote.setVisibleRowCount(12);

        JPanel pnlPulsanti = new JPanel(new GridLayout(1, 2, 4, 0));
        pnlPulsanti.add(new JButton("Nuova"));
        pnlPulsanti.add(new JButton("Elimina"));

        JPanel pnlElenco = new JPanel(new BorderLayout(0, 4));
        pnlElenco.add(new JScrollPane(lstNote), BorderLayout.CENTER);
        pnlElenco.add(pnlPulsanti, BorderLayout.SOUTH);

        // --- centro: titolo e testo
        JPanel pnlTitolo = new JPanel(new BorderLayout(4, 0));
        pnlTitolo.add(new JLabel("Titolo:"), BorderLayout.WEST);
        pnlTitolo.add(new JTextField(), BorderLayout.CENTER);

        JTextArea txtTesto = new JTextArea(15, 40);
        txtTesto.setLineWrap(true);
        txtTesto.setWrapStyleWord(true);

        JPanel pnlEditor = new JPanel(new BorderLayout(0, 4));
        pnlEditor.add(pnlTitolo, BorderLayout.NORTH);
        pnlEditor.add(new JScrollPane(txtTesto), BorderLayout.CENTER);

        // --- tutto insieme
        JLabel lblStato = new JLabel("4 note");

        JPanel pnlTutto = new JPanel(new BorderLayout(8, 4));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(pnlElenco, BorderLayout.WEST);
        pnlTutto.add(pnlEditor, BorderLayout.CENTER);
        pnlTutto.add(lblStato, BorderLayout.SOUTH);
        setContentPane(pnlTutto);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BloccoNote().setVisible(true));
    }
}
