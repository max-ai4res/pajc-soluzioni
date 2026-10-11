package it.unibs.pajc.redolfi.e09;

import java.util.ArrayDeque;
import java.util.Deque;

/** Il buffer a capienza fissa con wait e notifyAll della dispensa, reso generico. */
public class Buffer<T> {
    private final Deque<T> coda = new ArrayDeque<>();
    private final int capienza;

    public Buffer(int capienza) { this.capienza = capienza; }

    public synchronized void metti(T x) throws InterruptedException {
        while (coda.size() == capienza) wait();
        coda.addLast(x);
        notifyAll();
    }

    public synchronized T prendi() throws InterruptedException {
        while (coda.isEmpty()) wait();
        T x = coda.removeFirst();
        notifyAll();
        return x;
    }
}
