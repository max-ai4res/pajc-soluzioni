package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Il dialogo di connessione: host, porta e nome. Restituisce un Optional con i parametri
 * (vuoto se annullato) e non tocca la finestra che lo ha aperto.
 */
public class DialogConnessione extends JDialog {

    private static final long serialVersionUID = 1L;

    public record Parametri(String host, int porta, String nome) {}

    private final JTextField txtHost = new JTextField("localhost", 16);
    private final JTextField txtPorta = new JTextField("1234", 16);
    private final JTextField txtNome = new JTextField(16);
    private Parametri risultato;

    public DialogConnessione(Component proprietario) {
        super(proprietario == null ? null : SwingUtilities.getWindowAncestor(proprietario),
                "Connetti a WhatsPAJC", ModalityType.APPLICATION_MODAL);
        JPanel campi = new JPanel(new GridLayout(3, 2, 8, 6));
        campi.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        campi.add(new JLabel("Server:"));
        campi.add(txtHost);
        campi.add(new JLabel("Porta:"));
        campi.add(txtPorta);
        campi.add(new JLabel("Il tuo nome:"));
        campi.add(txtNome);

        JButton btnOk = new JButton("Connetti");
        JButton btnAnnulla = new JButton("Annulla");
        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pulsanti.add(btnAnnulla);
        pulsanti.add(btnOk);
        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> conferma());
        txtNome.addActionListener(e -> conferma());
        btnAnnulla.addActionListener(e -> dispose());

        add(campi, BorderLayout.CENTER);
        add(pulsanti, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(proprietario);
    }

    private void conferma() {
        String nome = txtNome.getText().strip();
        if (!Protocollo.nomeValido(nome)) {
            JOptionPane.showMessageDialog(this, "Il nome: da 2 a 16 lettere, cifre o _",
                    "Nome non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int porta;
        try {
            porta = Integer.parseInt(txtPorta.getText().strip());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La porta deve essere un numero",
                    "Porta non valida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        risultato = new Parametri(txtHost.getText().strip(), porta, nome);
        dispose();
    }

    /** Apre il dialogo e aspetta: i parametri, o vuoto se l'utente annulla. */
    public static Optional<Parametri> chiedi(Component proprietario) {
        DialogConnessione d = new DialogConnessione(proprietario);
        d.setVisible(true);                       // modale: torna alla chiusura
        return Optional.ofNullable(d.risultato);
    }

    /** Per le schermate: il dialogo gia' compilato, senza mostrarlo. */
    public static DialogConnessione esempio(String host, String porta, String nome) {
        DialogConnessione d = new DialogConnessione(null);
        d.txtHost.setText(host);
        d.txtPorta.setText(porta);
        d.txtNome.setText(nome);
        return d;
    }
}
