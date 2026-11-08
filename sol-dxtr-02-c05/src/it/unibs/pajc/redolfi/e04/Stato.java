package it.unibs.pajc.redolfi.e04;

public interface Stato {

    default void entra(SnakeApp app) { }

    default void stepNext(SnakeApp app, double dt) { }

    default void tasto(SnakeApp app, int codice) { }

    String messaggio();
}
