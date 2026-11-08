package it.unibs.pajc.redolfi.e01;

import javax.swing.JProgressBar;

/** L'altra vista: la barra con la percentuale. Stesso modello, altra traduzione. */
public class VistaBarra extends JProgressBar {

    public VistaBarra() {
        super(Contatore.MIN, Contatore.MAX);
        setStringPainted(true);
    }

    public void aggiorna(Contatore c) {
        setValue(c.valore());
        setString(c.valore() + " %");
    }
}
