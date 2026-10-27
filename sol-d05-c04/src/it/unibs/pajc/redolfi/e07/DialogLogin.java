package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Esercizio 4.7 - una finestra di login che restituisce un Optional<Credenziali>.
 *
 * Lo schema della sezione 4.5: un record per la risposta, OK che lo costruisce e chiude,
 * Annulla/Esc/X che chiudono e basta, chiedi() che mostra e restituisce. "Accedi" si
 * abilita solo con entrambi i campi pieni: un DocumentListener sul documento di ciascun
 * campo chiama aggiorna(), che ricalcola l'abilitazione da zero. DocumentListener ha tre
 * metodi e non ha un adapter: la piccola classe Cambiamento li fa confluire in uno.
 *
 * Errore tipico: abilitare "Accedi" con un KeyListener, che non vede il testo incollato con
 * il mouse ne' quello cancellato con un menu.
 */
public class DialogLogin extends JDialog {

    private static final long serialVersionUID = 1L;

    /** La password resta un char[]: equals del record la confronta per riferimento. */
    public record Credenziali(String nome, char[] password) { }

    private final JTextField txtNome = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);
    private final JButton btnAccedi = new JButton("Accedi");
    private Credenziali risultato;

    public DialogLogin(Window proprietario) {
        super(proprietario, "Accesso", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pnlModulo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlModulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        pnlModulo.add(new JLabel("Nome:"));
        pnlModulo.add(txtNome);
        pnlModulo.add(new JLabel("Password:"));
        pnlModulo.add(txtPassword);

        JButton btnAnnulla = new JButton("Annulla");
        JPanel pnlPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPulsanti.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        pnlPulsanti.add(btnAnnulla);
        pnlPulsanti.add(btnAccedi);

        add(pnlModulo, BorderLayout.CENTER);
        add(pnlPulsanti, BorderLayout.SOUTH);

        txtNome.getDocument().addDocumentListener(new Cambiamento(this::aggiorna));
        txtPassword.getDocument().addDocumentListener(new Cambiamento(this::aggiorna));
        btnAccedi.addActionListener(e -> {
            risultato = new Credenziali(txtNome.getText().strip(), txtPassword.getPassword());
            dispose();
        });
        btnAnnulla.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAccedi);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        aggiorna();

        pack();
        setLocationRelativeTo(proprietario);
    }

    public Optional<Credenziali> chiedi() {
        setVisible(true);
        return Optional.ofNullable(risultato);
    }

    private void aggiorna() {
        btnAccedi.setEnabled(!txtNome.getText().isBlank() && txtPassword.getPassword().length > 0);
    }

    /** Un DocumentListener che chiama la stessa azione per ogni cambiamento del testo. */
    private record Cambiamento(Runnable azione) implements DocumentListener {
        public void insertUpdate(DocumentEvent e) { azione.run(); }
        public void removeUpdate(DocumentEvent e) { azione.run(); }
        public void changedUpdate(DocumentEvent e) { azione.run(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Optional<Credenziali> c = new DialogLogin(null).chiedi();
            System.out.println(c.map(Credenziali::nome).orElse("annullato"));
        });
    }
}
