package it.unibs.pajc.redolfi.e02;

public class FuoriServizio implements Stato {

    @Override
    public void esegui(Distributore d, Comando c) {
        switch (c) {
            case Moneta m -> d.daiResto(m.centesimi());
            case Riparato r -> d.cambia(new InAttesa());
            case Scelta s -> d.messaggio("fuori servizio");
            case Resto r -> d.messaggio("fuori servizio");
            case Guasto g -> { }
        }
    }

    @Override
    public String nome() {
        return "fuori servizio";
    }
}
