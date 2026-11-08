package it.unibs.pajc.redolfi.e03;

import java.util.List;

/** Uno spostamento intero, da dove era a dove e' stata lasciata: creato al rilascio, non a ogni pixel. */
public record Sposta(Figura figura, double daX, double daY, double aX, double aY) implements Comando {

    @Override
    public void esegui(List<Figura> figure) {
        figura.spostaIn(aX, aY);
    }

    @Override
    public void annulla(List<Figura> figure) {
        figura.spostaIn(daX, daY);
    }
}
