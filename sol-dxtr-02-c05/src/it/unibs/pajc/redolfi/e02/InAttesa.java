package it.unibs.pajc.redolfi.e02;

public class InAttesa implements Stato {

    @Override
    public void esegui(Distributore d, Comando c) {
        switch (c) {
            case Moneta m -> {
                d.setCredito(m.centesimi());
                d.cambia(new ConCredito());
            }
            case Scelta s -> d.messaggio(s.prodotto() + ": " + Distributore.PREZZI.getOrDefault(s.prodotto(), 0) + " cent");
            case Guasto g -> d.cambia(new FuoriServizio());
            case Resto r -> { }
            case Riparato r -> { }
        }
    }

    @Override
    public String nome() {
        return "in attesa";
    }
}
