package it.unibs.pajc.redolfi.e02;

/** Uno stato del distributore: decide che cosa fa ogni comando e che cosa fa il tempo. */
public interface Stato {

    default void entra(Distributore d) { }

    default void stepNext(Distributore d, double dt) { }

    void esegui(Distributore d, Comando c);

    String nome();
}
