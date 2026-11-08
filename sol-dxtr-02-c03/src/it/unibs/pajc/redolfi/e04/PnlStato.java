package it.unibs.pajc.redolfi.e04;

import javax.swing.JLabel;

/** La riga di stato: una vista che si aggiorna da sola ascoltando il mondo. */
public class PnlStato extends JLabel implements MondoListener {

    private int lasciate, ferme;

    public PnlStato(Mondo mondo) {
        super("nessuna pallina");
        mondo.addMondoListener(this);
    }

    @Override
    public void pallinaLasciata(Pallina p) {
        lasciate++;
        aggiorna();
    }

    @Override
    public void pallinaFerma(Pallina p) {
        ferme++;
        aggiorna();
    }

    private void aggiorna() {
        setText(lasciate + " palline, " + ferme + " ferme");
    }
}
