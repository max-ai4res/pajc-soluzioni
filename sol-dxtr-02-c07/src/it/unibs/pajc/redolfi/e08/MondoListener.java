package it.unibs.pajc.redolfi.e08;

public interface MondoListener {

    default void pallinaLasciata(Pallina p) { }

    default void rimbalzo(Rimbalzo r) { }

    default void pallinaFerma(Pallina p) { }

    default void pallinaScoppiata(Pallina p) { }

    /** Il mondo ha fatto avanzare il tempo: chi campiona nel tempo del mondo si regola qui. */
    default void avanzato(double tempo) { }
}
