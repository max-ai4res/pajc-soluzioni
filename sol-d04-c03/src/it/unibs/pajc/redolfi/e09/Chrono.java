package it.unibs.pajc.redolfi.e09;

/** Cronometro in millisecondi dalla costruzione. */
public class Chrono {
    private final long t0 = System.nanoTime();
    public long millis() { return (System.nanoTime() - t0) / 1_000_000; }
}
