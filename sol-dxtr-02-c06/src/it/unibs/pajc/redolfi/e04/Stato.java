package it.unibs.pajc.redolfi.e04;

/**
 * Uno stato del gioco: dice che cosa fanno il tempo, i click e i tasti finche' si e' li'.
 * Le transizioni le chiede allo stesso controllore con cambia(). Di default niente
 * succede: ogni stato ridefinisce solo cio' che lo riguarda.
 */
public interface Stato {

    default void entra(Controllore c) { }

    default void stepNext(Controllore c, double dt) { }

    default void punto(Controllore c, Vec2 p) { }

    default void tasto(Controllore c, int codice) { }

    /** Il messaggio che la vista mostra in questo stato (null: nessuno). */
    String messaggio();
}
