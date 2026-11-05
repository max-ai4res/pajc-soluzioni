package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Esercizio 6.8 - il dialogo di connessione con il pulsante Cerca: Scoperta.cerca in un
 * SwingWorker (grida in UDP e aspetta fino a un secondo e mezzo: non sull'EDT); se trova
 * il server riempie host e porta, altrimenti lo dice nell'etichetta. Il metodo statico
 * chiedi(proprietario, cercaSubito) fa partire la ricerca da solo all'apertura: e' quello
 * che ClientApp usa all'avvio, cosi' l'utente trova host e porta gia' compilati e deve
 * solo scrivere il nome.
 *
 * Errore tipico: chiamare Scoperta.cerca nel listener del pulsante: la finestra congela
 * per tutta l'attesa, che con nessun server in rete e' l'intero timeout.
 */
public class DialogConnessione extends JDialog {

    private static final long serialVersionUID = 1L;

    public record Parametri(String host, int porta, String nome) {}

    private final JTextField txtHost = new JTextField("localhost", 16);
    private final JTextField txtPorta = new JTextField("1234", 16);
    private final JTextField txtNome = new JTextField(16);
    private final JButton btnCerca = new JButton("Cerca il server");
    private final JLabel lblCerca = new JLabel(" ");
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

        JPanel ricerca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ricerca.add(btnCerca);
        ricerca.add(lblCerca);
        btnCerca.addActionListener(e -> cerca());

        JButton btnOk = new JButton("Connetti");
        JButton btnAnnulla = new JButton("Annulla");
        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pulsanti.add(btnAnnulla);
        pulsanti.add(btnOk);
        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> conferma());
        txtNome.addActionListener(e -> conferma());
        btnAnnulla.addActionListener(e -> dispose());

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(campi, BorderLayout.CENTER);
        centro.add(ricerca, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        add(pulsanti, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(proprietario);
    }

    /** Grida in UDP da un SwingWorker e, se un server risponde, compila host e porta. */
    private void cerca() {
        btnCerca.setEnabled(false);
        lblCerca.setText("cerco...");
        new SwingWorker<Optional<String>, Void>() {
            @Override
            protected Optional<String> doInBackground() throws IOException {
                return Scoperta.cerca(1500);
            }

            @Override
            protected void done() {
                btnCerca.setEnabled(true);
                try {
                    Optional<String> trovato = get();
                    if (trovato.isPresent()) {
                        String[] hp = trovato.get().split(":");
                        txtHost.setText(hp[0]);
                        txtPorta.setText(hp[1]);
                        lblCerca.setText("trovato " + trovato.get());
                        txtNome.requestFocusInWindow();
                    } else {
                        lblCerca.setText("nessun server ha risposto");
                    }
                } catch (Exception e) {
                    lblCerca.setText("errore: " + e.getCause().getMessage());
                }
            }
        }.execute();
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
        return chiedi(proprietario, false);
    }

    /** Con cercaSubito la ricerca parte da sola all'apertura del dialogo. */
    public static Optional<Parametri> chiedi(Component proprietario, boolean cercaSubito) {
        DialogConnessione d = new DialogConnessione(proprietario);
        if (cercaSubito) d.cerca();
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
