package it.unibs.pajc.redolfi.e01;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/** Una vista: l'etichetta grande con il numero. Legge il modello quando glielo si chiede. */
public class VistaNumero extends JLabel {

    public VistaNumero() {
        super("0", SwingConstants.CENTER);
        setFont(getFont().deriveFont(Font.BOLD, 48f));
    }

    public void aggiorna(Contatore c) {
        setText(String.valueOf(c.valore()));
    }
}
