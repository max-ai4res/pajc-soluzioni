package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Esercizio 8.8 - la finestra del gioco della vita.
 *
 * Lo slider va da lento a veloce: l'intervallo del timer e' l'inverso, 1000 - valore.
 * La regola di Conway e' quella predefinita; per provarne un'altra basta passare una lambda,
 * per esempio "HighLife": (viva, n) -> n == 3 || n == 6 && !viva || viva && n == 2.
 */
public class Vita extends JFrame {

    private static final long serialVersionUID = 1L;

    public Vita() {
        super("Il gioco della vita");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        PnlVita pnl = new PnlVita(40, 60);
        pnl.setPreferredSize(new Dimension(720, 480));

        JButton btnAvvia = new JButton("Avvia");
        JButton btnSvuota = new JButton("Svuota");
        JButton btnCaso = new JButton("A caso");
        JSlider sldVelocita = new JSlider(0, 980, 880);
        btnAvvia.addActionListener(e -> {
            pnl.avviaFerma();
            btnAvvia.setText(pnl.inCorso() ? "Ferma" : "Avvia");
        });
        btnSvuota.addActionListener(e -> pnl.svuota());
        btnCaso.addActionListener(e -> pnl.aCaso());
        sldVelocita.addChangeListener(e -> pnl.setIntervallo(1000 - sldVelocita.getValue()));
        pnl.setIntervallo(1000 - sldVelocita.getValue());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(btnAvvia);
        barra.add(btnSvuota);
        barra.add(btnCaso);
        barra.add(new JLabel("Velocita':"));
        barra.add(sldVelocita);
        add(barra, BorderLayout.NORTH);
        add(pnl, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Vita().setVisible(true));
    }
}
