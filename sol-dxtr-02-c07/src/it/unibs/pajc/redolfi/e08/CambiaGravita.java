package it.unibs.pajc.redolfi.e08;

/** Cambia la gravita. Annullare = rimettere quella di prima, che esegui si e' segnata. */
public class CambiaGravita implements Comando {

    private final double nuova;
    private double vecchia;

    public CambiaGravita(double nuova) {
        this.nuova = nuova;
    }

    @Override
    public void esegui(Mondo mondo) {
        vecchia = mondo.gravita();
        mondo.setGravita(nuova);
    }

    @Override
    public void annulla(Mondo mondo) {
        mondo.setGravita(vecchia);
    }

    @Override
    public boolean annullabile() {
        return true;
    }
}
