package it.unibs.pajc.redolfi.e04;

/** Chi vuole sapere che cosa succede nel mondo. Ogni metodo ha un corpo vuoto: si ridefinisce solo cio' che interessa. */
public interface MondoListener {

    default void pallinaLasciata(Pallina p) { }

    default void rimbalzo(Rimbalzo r) { }

    default void pallinaFerma(Pallina p) { }
}
