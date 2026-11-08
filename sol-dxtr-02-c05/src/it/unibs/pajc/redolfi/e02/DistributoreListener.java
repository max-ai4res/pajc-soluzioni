package it.unibs.pajc.redolfi.e02;

public interface DistributoreListener {

    default void creditoCambiato(int centesimi) { }

    default void prodottoErogato(String prodotto) { }

    default void restoDato(int centesimi) { }

    default void messaggio(String testo) { }

    default void statoCambiato(String nome) { }
}
