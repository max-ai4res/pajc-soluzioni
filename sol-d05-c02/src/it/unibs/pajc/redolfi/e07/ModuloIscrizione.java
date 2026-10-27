package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Esercizio 2.7 - un modulo con etichette e campi allineati.
 *
 * Due colonne, ciascuna un GridLayout(0, 1): le celle di una colonna hanno tutte la stessa
 * altezza, e le due colonne hanno lo stesso numero di righe, quindi etichetta e campo di
 * ogni riga si trovano alla stessa altezza. Le colonne stanno in un BorderLayout: etichette
 * a WEST (larghe quanto la piu' lunga), campi al CENTER (che si allargano). Il pannello del
 * modulo va nel NORTH di un altro BorderLayout, cosi' riceve la sua altezza preferita e non
 * cresce in altezza; i pulsanti stanno in un FlowLayout allineato a destra a SOUTH.
 *
 * Errore tipico: il modulo al CENTER della finestra, con i campi che si allungano in
 * altezza fino a diventare enormi; oppure un unico GridLayout(0, 2), che da' alle etichette
 * meta' della larghezza.
 */
public class ModuloIscrizione extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String[] ETICHETTE = { "Nome:", "Cognome:", "Email:", "Anno di nascita:" };

    public ModuloIscrizione() {
        super("Iscrizione");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pnlEtichette = new JPanel(new GridLayout(0, 1, 0, 6));
        JPanel pnlCampi = new JPanel(new GridLayout(0, 1, 0, 6));
        for (String e : ETICHETTE) {
            pnlEtichette.add(new JLabel(e, SwingConstants.RIGHT));
            pnlCampi.add(new JTextField(20));
        }

        JPanel pnlModulo = new JPanel(new BorderLayout(8, 0));
        pnlModulo.add(pnlEtichette, BorderLayout.WEST);
        pnlModulo.add(pnlCampi, BorderLayout.CENTER);

        JPanel pnlPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlPulsanti.add(new JButton("Annulla"));
        pnlPulsanti.add(new JButton("Iscrivi"));

        JPanel pnlTutto = new JPanel(new BorderLayout(0, 12));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(pnlModulo, BorderLayout.NORTH);     // non cresce in altezza
        pnlTutto.add(pnlPulsanti, BorderLayout.SOUTH);   // sempre in fondo
        setContentPane(pnlTutto);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModuloIscrizione().setVisible(true));
    }
}
