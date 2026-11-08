package it.unibs.pajc.redolfi.e04;

import java.awt.event.KeyEvent;

/** All'inizio: il mondo e' fermo e si aspetta un tasto. */
public class Menu implements Stato {

    @Override
    public void tasto(Controllore c, int codice) {
        if (codice == KeyEvent.VK_SPACE)
            c.cambia(new InCorso());
    }

    @Override
    public String messaggio() {
        return "Palline  -  spazio per cominciare";
    }
}
