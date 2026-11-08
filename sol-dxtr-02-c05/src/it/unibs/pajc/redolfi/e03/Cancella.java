package it.unibs.pajc.redolfi.e03;

import java.util.List;

/** Cancella una figura ricordando dov'era nell'elenco, cosi' annullare la rimette al suo posto. */
public class Cancella implements Comando {

    private final Figura figura;
    private int indice;

    public Cancella(Figura figura) {
        this.figura = figura;
    }

    @Override
    public void esegui(List<Figura> figure) {
        indice = figure.indexOf(figura);
        figure.remove(indice);
    }

    @Override
    public void annulla(List<Figura> figure) {
        figure.add(indice, figura);
    }
}
