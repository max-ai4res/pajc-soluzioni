package it.unibs.pajc.redolfi.e08;

import java.awt.event.KeyEvent;

/** Il gioco vero: il tempo passa, i click lasciano palline, i tasti sono comandi. */
public class InCorso implements Stato {

    @Override
    public void stepNext(Controllore c, double dt) {
        c.mondo().stepNext(dt);
    }

    @Override
    public void punto(Controllore c, Vec2 p) {
        c.esegui(new Lascia(p, 0.25, c.materiale()));
    }

    @Override
    public void tasto(Controllore c, int codice) {
        switch (codice) {
            case KeyEvent.VK_SPACE -> c.cambia(new Pausa());
            case KeyEvent.VK_R -> c.cambia(new Riproduzione());
            case KeyEvent.VK_Z -> c.annulla();
            case KeyEvent.VK_C -> c.esegui(new Svuota());
            case KeyEvent.VK_1 -> c.setMateriale(new Gomma());
            case KeyEvent.VK_2 -> c.setMateriale(new Piombo());
            case KeyEvent.VK_3 -> c.setMateriale(new Bolla());
            default -> { }
        }
    }

    @Override
    public String messaggio() {
        return null;
    }
}
