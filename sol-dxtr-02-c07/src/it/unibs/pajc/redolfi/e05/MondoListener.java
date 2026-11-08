package it.unibs.pajc.redolfi.e05;

public interface MondoListener {

    default void pallinaLasciata(Pallina p) { }

    default void rimbalzo(Rimbalzo r) { }

    default void pallinaFerma(Pallina p) { }

    default void imbucata(Pallina p) { }
}
