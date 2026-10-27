package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.6 - un contatore con tre pulsanti e un solo listener.
 *
 * Il listener guarda il comando dell'evento (il testo del pulsante) e cambia lo stato,
 * cioe' il campo valore. Poi chiama aggiorna(), che ricalcola TUTTO cio' che si vede: il
 * testo dell'etichetta e l'abilitazione di "-1". Cosi' l'abilitazione e' giusta qualunque
 * pulsante sia stato premuto, e "Azzera" la sistema senza bisogno di pensarci.
 *
 * Errore tipico: disabilitare "-1" solo nel ramo di "-1", e non riabilitarlo mai.
 */
public class Contatore extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JLabel lblValore = new JLabel("0", SwingConstants.CENTER);
    private final JButton btnMeno = new JButton("-1");
    private int valore;

    public Contatore() {
        super("Contatore");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        lblValore.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 64));
        lblValore.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        JButton btnPiu = new JButton("+1");
        JButton btnAzzera = new JButton("Azzera");
        JPanel pnlPulsanti = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        for (JButton b : new JButton[] { btnMeno, btnPiu, btnAzzera }) {
            b.addActionListener(this::premuto);      // lo stesso listener per tutti
            pnlPulsanti.add(b);
        }

        add(lblValore, BorderLayout.CENTER);
        add(pnlPulsanti, BorderLayout.SOUTH);
        aggiorna();
        pack();
        setLocationRelativeTo(null);
    }

    private void premuto(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "+1" -> valore++;
            case "-1" -> valore = Math.max(0, valore - 1);
            case "Azzera" -> valore = 0;
            default -> throw new IllegalStateException(e.getActionCommand());
        }
        aggiorna();
    }

    private void aggiorna() {
        lblValore.setText(Integer.toString(valore));
        btnMeno.setEnabled(valore > 0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Contatore().setVisible(true));
    }
}
