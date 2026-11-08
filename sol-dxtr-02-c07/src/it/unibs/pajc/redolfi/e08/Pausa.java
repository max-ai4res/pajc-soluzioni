package it.unibs.pajc.redolfi.e08;

import java.awt.event.KeyEvent;

/** In pausa: il tempo non passa, i click non fanno niente, spazio riprende. */
public class Pausa implements Stato {

    @Override
    public void tasto(Controllore c, int codice) {
        if (codice == KeyEvent.VK_SPACE)
            c.cambia(new InCorso());
    }

    @Override
    public String messaggio() {
        return "pausa";
    }
}
