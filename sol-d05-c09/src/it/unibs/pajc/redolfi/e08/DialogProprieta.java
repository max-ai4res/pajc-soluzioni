package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

/**
 * La finestra "Proprieta'" della Lavagna (capitolo 4): modale, parte dai valori attuali,
 * restituisce la scelta come valore. Non tocca la finestra che l'ha aperta.
 */
public class DialogProprieta extends JDialog {

    private static final long serialVersionUID = 1L;

    /** Le proprieta' del disegno: un valore, quindi un record. */
    public record Proprieta(boolean griglia, int passo, Color sfondo) { }

    private final JCheckBox chkGriglia = new JCheckBox("Mostra la griglia");
    private final JSpinner spnPasso =
            new JSpinner(new SpinnerNumberModel(20, 5, 100, 5));
    private final JButton btnSfondo = new JButton();
    private Color sfondo;
    private Proprieta risultato;            // null se l'utente annulla

    public DialogProprieta(Window proprietario, Proprieta iniziali) {
        super(proprietario, "Proprietà", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // la X annulla: risultato resta null

        chkGriglia.setSelected(iniziali.griglia());
        spnPasso.setValue(iniziali.passo());
        spnPasso.setEnabled(iniziali.griglia());
        sfondo = iniziali.sfondo();
        aggiornaSfondo();

        JButton btnAnnulla = new JButton("Annulla");
        JButton btnOk = new JButton("OK");
        add(creaModulo(), BorderLayout.CENTER);
        add(creaPulsanti(btnAnnulla, btnOk), BorderLayout.SOUTH);

        chkGriglia.addActionListener(e -> spnPasso.setEnabled(chkGriglia.isSelected()));
        btnSfondo.addActionListener(e -> scegliSfondo());
        btnOk.addActionListener(e -> {
            risultato = new Proprieta(chkGriglia.isSelected(), (Integer) spnPasso.getValue(), sfondo);
            dispose();                              // chiedi() riprende da qui
        });
        btnAnnulla.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnOk);      // Invio = OK
        getRootPane().registerKeyboardAction(e -> dispose(),     // Esc = Annulla
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(proprietario);
    }

    /** Mostra la finestra e aspetta: vuoto se l'utente ha annullato. */
    public Optional<Proprieta> chiedi() {
        setVisible(true);                           // modale: torna alla chiusura
        return Optional.ofNullable(risultato);
    }

    private JPanel creaModulo() {
        JPanel pnlModulo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlModulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        pnlModulo.add(new JLabel("Griglia:"));
        pnlModulo.add(chkGriglia);
        pnlModulo.add(new JLabel("Passo (pixel):"));
        pnlModulo.add(spnPasso);
        pnlModulo.add(new JLabel("Sfondo:"));
        pnlModulo.add(btnSfondo);
        return pnlModulo;
    }

    private JPanel creaPulsanti(JButton btnAnnulla, JButton btnOk) {
        JPanel pnlPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPulsanti.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        pnlPulsanti.add(btnAnnulla);
        pnlPulsanti.add(btnOk);
        return pnlPulsanti;
    }

    private void scegliSfondo() {
        // un dialogo sopra un dialogo: il proprietario e' questa finestra, non la Lavagna
        Color c = JColorChooser.showDialog(this, "Colore dello sfondo", sfondo);
        if (c != null) {
            sfondo = c;
            aggiornaSfondo();
        }
    }

    private void aggiornaSfondo() {
        btnSfondo.setIcon(new IconaColore(sfondo, 12));
        btnSfondo.setText(String.format("#%02X%02X%02X", sfondo.getRed(), sfondo.getGreen(), sfondo.getBlue()));
    }
}
