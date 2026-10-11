package it.unibs.pajc.redolfi.e09;

/** Un compito che conta i primi in [from, to] e tiene il risultato e il tempo nei campi. */
public class PrimeCounter implements Runnable {
    private final long from, to;
    private long result, millis;

    public PrimeCounter(long from, long to) { this.from = from; this.to = to; }

    public void run() {
        Chrono chrono = new Chrono();
        result = PrimeTester.count(from, to);
        millis = chrono.millis();
    }

    public long result() { return result; }
    public long millis() { return millis; }
    public String toString() { return String.format("[%d, %d]", from, to); }
}
