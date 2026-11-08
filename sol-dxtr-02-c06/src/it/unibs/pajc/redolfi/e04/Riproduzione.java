package it.unibs.pajc.redolfi.e04;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * La riproduzione: il mondo riparte da zero e i comandi registrati vengono rieseguiti agli
 * stessi istanti di tempo del mondo. Funziona perche' la fisica e' a passo fisso: lo stesso
 * mondo con gli stessi comandi agli stessi istanti fa esattamente le stesse cose.
 */
public class Riproduzione implements Stato {

    private List<Controllore.Registrato> nastro;
    private int prossimo;

    @Override
    public void entra(Controllore c) {
        nastro = c.nastro();
        prossimo = 0;
        c.ricomincia();
    }

    @Override
    public void stepNext(Controllore c, double dt) {
        Mondo mondo = c.mondo();
        mondo.stepNext(dt);
        while (prossimo < nastro.size()
                && nastro.get(prossimo).tempo() <= mondo.tempo()) {
            nastro.get(prossimo).comando().esegui(mondo);
            prossimo++;
        }
        boolean finito = prossimo >= nastro.size()
                && mondo.palline().stream().allMatch(Pallina::ferma);
        if (finito)
            c.cambia(new InCorso());                     // si torna a giocare
    }

    @Override
    public void tasto(Controllore c, int codice) {
        if (codice == KeyEvent.VK_ESCAPE)
            c.cambia(new InCorso());
    }

    @Override
    public String messaggio() {
        return "riproduzione  -  Esc interrompe";
    }
}
