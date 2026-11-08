package it.unibs.pajc.redolfi.e02;

public class ConCredito implements Stato {

    @Override
    public void esegui(Distributore d, Comando c) {
        switch (c) {
            case Moneta m -> d.setCredito(d.credito() + m.centesimi());
            case Scelta s -> {
                Integer prezzo = Distributore.PREZZI.get(s.prodotto());
                if (prezzo == null)
                    d.messaggio("prodotto sconosciuto");
                else if (prezzo > d.credito())
                    d.messaggio("mancano " + (prezzo - d.credito()) + " cent");
                else {
                    d.setCredito(d.credito() - prezzo);
                    d.cambia(new InErogazione(s.prodotto()));
                }
            }
            case Resto r -> {
                d.daiResto(d.credito());
                d.setCredito(0);
                d.cambia(new InAttesa());
            }
            case Guasto g -> {
                d.daiResto(d.credito());
                d.setCredito(0);
                d.cambia(new FuoriServizio());
            }
            case Riparato r -> { }
        }
    }

    @Override
    public String nome() {
        return "con credito";
    }
}
