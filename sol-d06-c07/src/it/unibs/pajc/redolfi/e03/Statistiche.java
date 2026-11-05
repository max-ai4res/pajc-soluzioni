package it.unibs.pajc.redolfi.e03;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lo stato condiviso fra tutte le sessioni: le operazioni eseguite in tutto e i client
 * collegati adesso. LongAdder per il contatore che molti thread incrementano e pochi
 * leggono; AtomicInteger per i collegati, aggiornato in entrata e nel finally. Il server
 * lo crea e lo passa a ogni Sessione; i test ne usano uno loro.
 */
public class Statistiche {
    private final LongAdder operazioni = new LongAdder();
    private final AtomicInteger collegati = new AtomicInteger();

    public void operazione() { operazioni.increment(); }
    public void entra() { collegati.incrementAndGet(); }
    public void esce() { collegati.decrementAndGet(); }

    public long operazioni() { return operazioni.sum(); }
    public int collegati() { return collegati.get(); }
}
