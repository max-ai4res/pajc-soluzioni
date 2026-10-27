package it.unibs.pajc.redolfi.e03;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Problema 9.3 - il convertitore di lunghezze.
 *
 * Le unita' sono record (nome, metri per unita'); le tendine contengono gli oggetti, e la
 * conversione passa per il metro: valore * da.metri() / a.metri(). Un solo metodo
 * converti() ricalcola il risultato ed e' chiamato da tutti i listener: il
 * DocumentListener del campo (a ogni carattere), le due tendine, lo scambio. Un valore non
 * numerico produce un trattino; l'Invio nel campo (ActionListener del JTextField) mostra
 * in piu' un messaggio d'errore.
 *
 * Errore tipico: Double.parseDouble senza try, che lancia NumberFormatException a ogni
 * carattere non numerico (anche il "-" iniziale mentre si scrive un negativo).
 */
public class Convertitore extends JFrame {

    private static final long serialVersionUID = 1L;

    record Unita(String nome, double metri) {
        @Override public String toString() { return nome; }
    }

    static final Unita[] UNITA = { new Unita("metri", 1), new Unita("chilometri", 1000),
            new Unita("centimetri", 0.01), new Unita("miglia", 1609.344),
            new Unita("piedi", 0.3048), new Unita("pollici", 0.0254) };

    private final JTextField txtValore = new JTextField("1", 10);
    private final JComboBox<Unita> cmbDa = new JComboBox<>(UNITA);
    private final JComboBox<Unita> cmbA = new JComboBox<>(UNITA);
    private final JLabel lblRisultato = new JLabel("", SwingConstants.CENTER);

    public Convertitore() {
        super("Convertitore");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        cmbA.setSelectedIndex(4);
        JButton btnScambia = new JButton("<->");

        JPanel pnlNord = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        pnlNord.add(txtValore);
        pnlNord.add(cmbDa);
        pnlNord.add(btnScambia);
        pnlNord.add(cmbA);
        lblRisultato.setFont(lblRisultato.getFont().deriveFont(Font.BOLD, 28f));
        lblRisultato.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));
        add(pnlNord, BorderLayout.NORTH);
        add(lblRisultato, BorderLayout.CENTER);

        txtValore.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { converti(); }
            public void removeUpdate(DocumentEvent e) { converti(); }
            public void changedUpdate(DocumentEvent e) { converti(); }
        });
        txtValore.addActionListener(e -> {
            if (valore() == null)
                JOptionPane.showMessageDialog(this, "\"" + txtValore.getText() + "\" non è un numero.",
                        "Convertitore", JOptionPane.ERROR_MESSAGE);
        });
        cmbDa.addActionListener(e -> converti());
        cmbA.addActionListener(e -> converti());
        btnScambia.addActionListener(e -> {
            Object da = cmbDa.getSelectedItem();
            cmbDa.setSelectedItem(cmbA.getSelectedItem());
            cmbA.setSelectedItem(da);
        });
        converti();
        pack();
        setLocationRelativeTo(null);
    }

    private Double valore() {
        try {
            return Double.parseDouble(txtValore.getText().strip().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void converti() {
        Double v = valore();
        Unita da = (Unita) cmbDa.getSelectedItem(), a = (Unita) cmbA.getSelectedItem();
        lblRisultato.setText(v == null ? "-" : String.format("%.4g %s", v * da.metri() / a.metri(), a.nome()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Convertitore().setVisible(true));
    }
}
