package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.DoubleBinaryOperator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Il dialogo modale "Nuovo operatore": nome, simbolo e corpo in a e b. OK compila in un
 * SwingWorker; se compila il dialogo si chiude e chiedi() restituisce l'operatore, se
 * no l'errore compare sotto il corpo e il dialogo resta aperto.
 */
public class DlgNuovoOperatore extends JDialog {

    public record Operatore(String nome, String simbolo, String corpo, DoubleBinaryOperator op) { }

    private final JTextField txtNome = new JTextField(12), txtSimbolo = new JTextField(4);
    private final JTextArea txtCorpo = new JTextArea(5, 30);
    private final JLabel lblErrore = new JLabel(" ");
    private final JButton btnOk = new JButton("OK");
    private final CompilatoreOperatori compilatore;
    private Operatore risultato;

    public DlgNuovoOperatore(Frame proprietario, CompilatoreOperatori compilatore) {
        super(proprietario, "Nuovo operatore", ModalityType.APPLICATION_MODAL);
        this.compilatore = compilatore;
        JPanel campi = new JPanel(new GridLayout(2, 2, 4, 4));
        campi.add(new JLabel("Nome"));
        campi.add(txtNome);
        campi.add(new JLabel("Simbolo (tasto)"));
        campi.add(txtSimbolo);
        txtCorpo.setText("Math.pow(a, b)");
        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(campi, BorderLayout.NORTH);
        centro.add(new JLabel("f(a, b) ="), BorderLayout.WEST);
        centro.add(new JScrollPane(txtCorpo), BorderLayout.CENTER);
        lblErrore.setForeground(Color.RED);
        centro.add(lblErrore, BorderLayout.SOUTH);
        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAnnulla = new JButton("Annulla");
        btnAnnulla.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> compila());
        pulsanti.add(btnAnnulla);
        pulsanti.add(btnOk);
        add(centro, BorderLayout.CENTER);
        add(pulsanti, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);
        pack();
        setLocationRelativeTo(proprietario);
    }

    /** Mostra il dialogo e torna alla chiusura: l'operatore, o vuoto se annullato. */
    public Optional<Operatore> chiedi() {
        setVisible(true);                                    // modale: torna quando si chiude
        return Optional.ofNullable(risultato);
    }

    private void compila() {
        String nome = txtNome.getText().strip(), simbolo = txtSimbolo.getText().strip(), corpo = txtCorpo.getText();
        if (nome.isEmpty() || simbolo.isEmpty() || corpo.isBlank()) {
            lblErrore.setText("nome, simbolo e corpo sono obbligatori");
            return;
        }
        btnOk.setEnabled(false);
        lblErrore.setText("compilo...");
        new SwingWorker<DoubleBinaryOperator, Void>() {
            @Override
            protected DoubleBinaryOperator doInBackground() throws Exception {
                return compilatore.compila(corpo);
            }

            @Override
            protected void done() {
                btnOk.setEnabled(true);
                try {
                    risultato = new Operatore(nome, simbolo, corpo, get());
                    dispose();                               // solo se compila
                } catch (ExecutionException e) {
                    lblErrore.setText(e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
