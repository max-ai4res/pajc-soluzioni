package it.unibs.pajc.redolfi.e03;

public interface SpesaListener {

    default void aggiunta(int indice, Voce v) { }

    default void rimossa(int indice, Voce v) { }

    default void cambiata(int indice, Voce v) { }
}
