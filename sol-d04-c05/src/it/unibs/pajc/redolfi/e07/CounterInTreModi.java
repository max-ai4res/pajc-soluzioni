package it.unibs.pajc.redolfi.e07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 5.7: Counter in tre modi.
 *
 *  1. CounterSync: metodo synchronized, anche get(). La scelta quando il contatore e'
 *     uno dei campi di un oggetto con altro stato da aggiornare insieme: il monitor
 *     protegge uno stato, non una variabile.
 *  2. CounterLock: blocco synchronized su un lock privato. Stesso comportamento, ma
 *     nessuno dall'esterno puo' fare synchronized (counter) e interferire: la scelta per
 *     una classe esposta a molti.
 *  3. Nessuna condivisione: ogni compito conta in una variabile locale (confinata nel
 *     suo thread) e restituisce il totale; main somma. Nessun lock, nessuna contesa: la
 *     scelta quando il problema si divide, cioe' quasi sempre nel calcolo.
 *
 * Errore tipico: get() senza synchronized nelle prime due: compila, quasi sempre
 * restituisce il valore giusto, ma non e' garantito che veda l'ultima scrittura.
 */
public class CounterInTreModi {

    static class CounterSync {
        private int count;
        synchronized void inc() { count++; }
        synchronized int get() { return count; }
    }

    static class CounterLock {
        private final Object lock = new Object();
        private int count;
        void inc() { synchronized (lock) { count++; } }
        int get()  { synchronized (lock) { return count; } }
    }

    public static void main(String[] args) throws Exception {
        int n = 10_000, nThread = 4;

        CounterSync c1 = new CounterSync();
        try (ExecutorService ex = Executors.newFixedThreadPool(nThread)) {
            for (int i = 0; i < n; i++) ex.execute(c1::inc);
        }
        System.out.println("synchronized:      " + c1.get());

        CounterLock c2 = new CounterLock();
        try (ExecutorService ex = Executors.newFixedThreadPool(nThread)) {
            for (int i = 0; i < n; i++) ex.execute(c2::inc);
        }
        System.out.println("lock privato:      " + c2.get());

        // 3. nessuna condivisione: nThread compiti, ciascuno fa n/nThread incrementi locali
        long totale = 0;
        try (ExecutorService ex = Executors.newFixedThreadPool(nThread)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<Integer>>();
            for (int t = 0; t < nThread; t++)
                futures.add(ex.submit(() -> {
                    int locale = 0;                       // confinato nel thread
                    for (int i = 0; i < n / nThread; i++) locale++;
                    return locale;
                }));
            for (var f : futures) totale += f.get();
        }
        System.out.println("nessuna condiv.:   " + totale);
    }
}
