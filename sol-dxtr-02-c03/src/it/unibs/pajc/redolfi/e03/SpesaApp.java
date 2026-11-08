package it.unibs.pajc.redolfi.e03;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.3, la lista della spesa.
 *
 * Idea: Spesa e' il modello e annuncia aggiunta/rimossa/cambiata; SpesaListModel e' un
 * AbstractListModel che legge da Spesa e traduce i fatti in fireIntervalAdded e simili,
 * cosi' la JList ridisegna solo cio' che serve; l'etichetta del totale e' un secondo
 * listener. Il campo e i tre pulsanti sono il controllore.
 *
 * Errore tipico: un ListModel che tiene una copia delle voci. Prima o poi copia e modello
 * divergono; il ListModel qui non tiene niente, getElementAt legge da Spesa.
 */
public class SpesaApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Spesa spesa = new Spesa();
            JList<Voce> lista = new JList<>(new SpesaListModel(spesa));
            lista.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> l, Object o, int i, boolean sel, boolean foc) {
                    Voce v = (Voce) o;
                    super.getListCellRendererComponent(l, String.format("%s  %.2f", v.nome(), v.prezzo()), i, sel, foc);
                    setFont(getFont().deriveFont(v.presa() ? Font.ITALIC : Font.PLAIN));
                    setText(v.presa() ? "✓ " + getText() : getText());
                    return this;
                }
            });

            JLabel totale = new JLabel();
            Runnable aggiornaTotale = () -> totale.setText(String.format("da prendere: %.2f", spesa.totaleDaPrendere()));
            spesa.addSpesaListener(new SpesaListener() {
                @Override public void aggiunta(int i, Voce v) { aggiornaTotale.run(); }
                @Override public void rimossa(int i, Voce v)  { aggiornaTotale.run(); }
                @Override public void cambiata(int i, Voce v) { aggiornaTotale.run(); }
            });
            aggiornaTotale.run();

            JTextField campo = new JTextField(14);
            JButton aggiungi = new JButton("Aggiungi"), rimuovi = new JButton("Rimuovi"), segna = new JButton("Presa");
            aggiungi.addActionListener(e -> {
                String[] parti = campo.getText().trim().split("\\s+");
                if (parti.length == 2) {
                    spesa.aggiungi(parti[0], Double.parseDouble(parti[1].replace(',', '.')));
                    campo.setText("");
                }
            });
            rimuovi.addActionListener(e -> {
                if (lista.getSelectedIndex() >= 0)
                    spesa.rimuovi(lista.getSelectedIndex());
            });
            segna.addActionListener(e -> {
                int i = lista.getSelectedIndex();
                if (i >= 0)
                    spesa.segna(i, !spesa.voce(i).presa());
            });

            JPanel comandi = new JPanel(new FlowLayout());
            comandi.add(new JLabel("nome prezzo"));
            comandi.add(campo);
            comandi.add(aggiungi);
            comandi.add(rimuovi);
            comandi.add(segna);
            JFrame f = new JFrame("La spesa");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(comandi, BorderLayout.NORTH);
            f.add(new JScrollPane(lista), BorderLayout.CENTER);
            f.add(totale, BorderLayout.SOUTH);
            f.setSize(520, 360);
            f.setVisible(true);
            spesa.aggiungi("pane", 1.80);
            spesa.aggiungi("latte", 1.35);
            spesa.aggiungi("mele", 3.20);
        });
    }
}
