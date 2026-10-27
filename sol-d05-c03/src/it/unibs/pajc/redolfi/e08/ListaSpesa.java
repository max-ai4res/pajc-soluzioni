package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.8 - la lista della spesa.
 *
 * Le voci stanno in un DefaultListModel: aggiungere e togliere si fa sul modello, e la
 * lista, che ne e' un listener, si aggiorna da sola. Il conteggio e l'abilitazione di
 * "Rimuovi" sono invece parte di cio' che si vede e non dipendono da nessun modello: li
 * ricalcola aggiorna(), chiamato dopo ogni modifica e a ogni cambio di selezione.
 *
 * Lo stesso listener serve il pulsante "Aggiungi" e l'Invio nel campo di testo: un
 * JTextField genera un ActionEvent quando si preme Invio.
 *
 * Errore tipico: new JList<>(array), che non si puo' modificare.
 */
public class ListaSpesa extends JFrame {

    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> mdlVoci = new DefaultListModel<>();
    private final JList<String> lstVoci = new JList<>(mdlVoci);
    private final JTextField txtVoce = new JTextField(20);
    private final JButton btnRimuovi = new JButton("Rimuovi");
    private final JLabel lblConteggio = new JLabel();

    public ListaSpesa() {
        super("Lista della spesa");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton btnAggiungi = new JButton("Aggiungi");
        JPanel pnlNord = new JPanel(new BorderLayout(4, 0));
        pnlNord.add(txtVoce, BorderLayout.CENTER);
        pnlNord.add(btnAggiungi, BorderLayout.EAST);

        lstVoci.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstVoci.setVisibleRowCount(10);

        JPanel pnlSud = new JPanel(new BorderLayout(4, 0));
        pnlSud.add(lblConteggio, BorderLayout.CENTER);
        pnlSud.add(btnRimuovi, BorderLayout.EAST);

        JPanel pnlTutto = new JPanel(new BorderLayout(0, 6));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(pnlNord, BorderLayout.NORTH);
        pnlTutto.add(new JScrollPane(lstVoci), BorderLayout.CENTER);
        pnlTutto.add(pnlSud, BorderLayout.SOUTH);
        setContentPane(pnlTutto);

        btnAggiungi.addActionListener(e -> aggiungi());
        txtVoce.addActionListener(e -> aggiungi());     // Invio nel campo
        btnRimuovi.addActionListener(e -> rimuovi());
        lstVoci.addListSelectionListener(e -> aggiorna());
        aggiorna();

        pack();
        setLocationRelativeTo(null);
    }

    private void aggiungi() {
        String voce = txtVoce.getText().strip();
        if (voce.isEmpty() || presente(voce)) {
            txtVoce.selectAll();                          // da correggere: lo si seleziona
            return;
        }
        mdlVoci.addElement(voce);
        txtVoce.setText("");
        aggiorna();
    }

    private boolean presente(String voce) {
        for (int i = 0; i < mdlVoci.size(); i++)
            if (mdlVoci.get(i).equalsIgnoreCase(voce))
                return true;
        return false;
    }

    private void rimuovi() {
        int i = lstVoci.getSelectedIndex();
        if (i >= 0)
            mdlVoci.remove(i);
        aggiorna();
    }

    private void aggiorna() {
        btnRimuovi.setEnabled(lstVoci.getSelectedIndex() >= 0);
        int n = mdlVoci.size();
        lblConteggio.setText(n == 1 ? "1 voce" : n + " voci");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListaSpesa().setVisible(true));
    }
}
