package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Esercizio 6.6 - il tratteggio.
 *
 * Un solo metodo aggiorna() legge i quattro controlli e li passa al pannello, che costruisce
 * il BasicStroke in paintComponent. La fase sposta il punto di partenza del tratteggio:
 * facendola crescere con un timer si otterrebbero le "formiche in marcia".
 *
 * Errore tipico: impostare il tratto in paintComponent sul contesto ricevuto senza copiarlo,
 * che sporca il disegno successivo di Swing.
 */
public class Tratteggio extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JSlider sldSpessore = new JSlider(1, 20, 4);
    private final JSlider sldTrattino = new JSlider(2, 40, 12);
    private final JSlider sldFase = new JSlider(0, 40, 0);
    private final JCheckBox chkTondi = new JCheckBox("Estremi arrotondati");
    private final PnlTratto pnl = new PnlTratto();

    public Tratteggio() {
        super("Tratteggio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pnlControlli = new JPanel(new GridLayout(0, 2, 8, 2));
        pnlControlli.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        pnlControlli.add(new JLabel("Spessore"));
        pnlControlli.add(sldSpessore);
        pnlControlli.add(new JLabel("Trattino"));
        pnlControlli.add(sldTrattino);
        pnlControlli.add(new JLabel("Fase"));
        pnlControlli.add(sldFase);
        pnlControlli.add(new JLabel());
        pnlControlli.add(chkTondi);

        pnl.setPreferredSize(new Dimension(660, 220));
        add(pnlControlli, BorderLayout.NORTH);
        add(pnl, BorderLayout.CENTER);

        sldSpessore.addChangeListener(e -> aggiorna());
        sldTrattino.addChangeListener(e -> aggiorna());
        sldFase.addChangeListener(e -> aggiorna());
        chkTondi.addActionListener(e -> aggiorna());
        aggiorna();

        pack();
        setLocationRelativeTo(null);
    }

    private void aggiorna() {
        pnl.setTratto(sldSpessore.getValue(), sldTrattino.getValue(), sldFase.getValue(),
                chkTondi.isSelected());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tratteggio().setVisible(true));
    }
}
