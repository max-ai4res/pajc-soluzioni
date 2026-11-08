package it.unibs.pajc.redolfi.e04;

public interface MondoListener {

    default void pallinaLasciata(Pallina p) { }

    default void rimbalzo(Rimbalzo r) { }

    default void pallinaFerma(Pallina p) { }

    default void pallinaScoppiata(Pallina p) { }
}
