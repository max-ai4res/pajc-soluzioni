package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 5.9 - poligoni e stelle.
 *
 * La finestra ascolta i suoi componenti e passa i valori al pannello con setLati e
 * setStella; il pannello si ridisegna da solo. Il pannello non legge lo spinner: e' cosi'
 * che resta riusabile, e che il suo disegno dipende solo dal suo stato.
 *
 * Errore tipico: paintComponent che legge spinner.getValue(), legando il pannello a questa
 * finestra.
 */
public class Poligoni extends JFrame {

    private static final long serialVersionUID = 1L;

    public Poligoni() {
        super("Poligoni e stelle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        PnlPoligono pnl = new PnlPoligono();
        pnl.setPreferredSize(new Dimension(420, 420));
        JSpinner spnLati = new JSpinner(new SpinnerNumberModel(5, 3, 20, 1));
        JCheckBox chkStella = new JCheckBox("Stella");

        JPanel pnlBarra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnlBarra.add(new JLabel("Lati:"));
        pnlBarra.add(spnLati);
        pnlBarra.add(chkStella);
        add(pnlBarra, BorderLayout.NORTH);
        add(pnl, BorderLayout.CENTER);

        spnLati.addChangeListener(e -> pnl.setLati((Integer) spnLati.getValue()));
        chkStella.addActionListener(e -> pnl.setStella(chkStella.isSelected()));

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Poligoni().setVisible(true));
    }
}
