package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Esercizio 4.9 - la finestra "Trova", non modale.
 *
 * Una finestra non modale non restituisce un valore: setVisible(true) torna subito, e
 * l'utente continua a lavorare nell'editor. La finestra agisce quindi direttamente sul
 * componente di testo che riceve nel costruttore: cerca dal cursore in avanti, seleziona
 * l'occorrenza trovata e ricomincia dall'inizio quando arriva in fondo. "Chiudi", Esc e la
 * X la nascondono (HIDE_ON_CLOSE): l'editor la tiene in un campo e la rimostra, con
 * l'ultimo testo cercato. Qui riusare l'istanza e' proprio cio' che si vuole.
 *
 * Errore tipico: cercare sempre dall'inizio (si trova sempre la prima occorrenza), oppure
 * renderla modale, che impedisce di correggere il testo trovato senza chiuderla.
 */
public class DialogTrova extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextComponent testo;
    private final JTextField txtCerca = new JTextField(20);
    private final JCheckBox chkMaiuscole = new JCheckBox("Maiuscole e minuscole");

    public DialogTrova(Window proprietario, JTextComponent testo) {
        super(proprietario, "Trova", ModalityType.MODELESS);
        this.testo = testo;
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        JPanel pnlCampo = new JPanel(new BorderLayout(6, 4));
        pnlCampo.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        pnlCampo.add(new JLabel("Trova:"), BorderLayout.WEST);
        pnlCampo.add(txtCerca, BorderLayout.CENTER);
        pnlCampo.add(chkMaiuscole, BorderLayout.SOUTH);

        JButton btnTrova = new JButton("Trova successivo");
        JButton btnChiudi = new JButton("Chiudi");
        JPanel pnlPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPulsanti.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        pnlPulsanti.add(btnChiudi);
        pnlPulsanti.add(btnTrova);

        add(pnlCampo, BorderLayout.CENTER);
        add(pnlPulsanti, BorderLayout.SOUTH);

        btnTrova.addActionListener(e -> trovaSuccessivo());
        btnChiudi.addActionListener(e -> setVisible(false));
        getRootPane().setDefaultButton(btnTrova);
        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(proprietario);
    }

    /** Mostra la finestra con il campo selezionato, pronto per scrivere. */
    public void mostra() {
        setVisible(true);
        txtCerca.selectAll();
        txtCerca.requestFocusInWindow();
    }

    private void trovaSuccessivo() {
        String cercato = txtCerca.getText();
        if (cercato.isEmpty())
            return;
        String tutto = testo.getText();
        if (!chkMaiuscole.isSelected()) {
            cercato = cercato.toLowerCase(Locale.ROOT);
            tutto = tutto.toLowerCase(Locale.ROOT);
        }
        int da = testo.getSelectionEnd();               // dopo l'occorrenza gia' selezionata
        int i = tutto.indexOf(cercato, da);
        if (i < 0)
            i = tutto.indexOf(cercato);                 // ricomincia dall'inizio
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "\"" + txtCerca.getText() + "\" non c'è.",
                    "Trova", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        testo.select(i, i + cercato.length());
        testo.getCaret().setSelectionVisible(true);     // visibile anche senza il fuoco
    }
}
