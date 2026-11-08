package it.unibs.pajc.redolfi.e03;

import java.util.List;

public record Aggiungi(Figura figura) implements Comando {

    @Override
    public void esegui(List<Figura> figure) {
        figure.add(figura);
    }

    @Override
    public void annulla(List<Figura> figure) {
        figure.remove(figura);
    }
}
