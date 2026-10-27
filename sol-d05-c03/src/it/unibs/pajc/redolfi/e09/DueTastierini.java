package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.9 - due tastierini, un display.
 *
 * La finestra registra lo stesso listener su entrambi i tastierini e lavora solo sul
 * comando dell'evento: non sa che dentro ci sono dei pulsanti, e non le interessa quale
 * tastierino sia stato usato (se servisse, getSource() lo direbbe).
 */
public class DueTastierini extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTextField txtDisplay = new JTextField();

    public DueTastierini() {
        super("Due tastierini");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        txtDisplay.setEditable(false);
        txtDisplay.setHorizontalAlignment(JTextField.RIGHT);
        txtDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));

        PnlTastierino sinistro = new PnlTastierino();
        PnlTastierino destro = new PnlTastierino();
        sinistro.addActionListener(this::tasto);
        destro.addActionListener(this::tasto);

        JPanel pnlTastierini = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlTastierini.add(sinistro);
        pnlTastierini.add(destro);

        JPanel pnlTutto = new JPanel(new BorderLayout(0, 8));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(txtDisplay, BorderLayout.NORTH);
        pnlTutto.add(pnlTastierini, BorderLayout.CENTER);
        setContentPane(pnlTutto);

        pack();
        setLocationRelativeTo(null);
    }

    private void tasto(ActionEvent e) {
        String t = e.getActionCommand();
        if (t.equals("C"))
            txtDisplay.setText("");
        else
            txtDisplay.setText(txtDisplay.getText() + t);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DueTastierini().setVisible(true));
    }
}
