package it.unibs.pajc.redolfi.e02;

/** Dura tre secondi di tempo misurato; una moneta inserita adesso torna indietro. */
public class InErogazione implements Stato {

    private final String prodotto;
    private double trascorso;

    public InErogazione(String prodotto) {
        this.prodotto = prodotto;
    }

    @Override
    public void entra(Distributore d) {
        d.messaggio("erogazione di " + prodotto + "...");
    }

    @Override
    public void stepNext(Distributore d, double dt) {
        trascorso += dt;
        if (trascorso >= Distributore.DURATA_EROGAZIONE) {
            d.eroga(prodotto);
            d.cambia(d.credito() > 0 ? new ConCredito() : new InAttesa());
        }
    }

    @Override
    public void esegui(Distributore d, Comando c) {
        switch (c) {
            case Moneta m -> d.daiResto(m.centesimi());     // restituita subito
            case Guasto g -> d.cambia(new FuoriServizio());
            case Scelta s -> d.messaggio("attendere");
            case Resto r -> d.messaggio("attendere");
            case Riparato r -> { }
        }
    }

    @Override
    public String nome() {
        return "in erogazione";
    }
}
