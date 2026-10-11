package it.unibs.pajc.redolfi.e09;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Palestra 9.9: sedici thread chiedono le stesse dieci chiavi: dieci calcoli (piu' uno
 * per la chiave 7, che fallisce la prima volta e viene ricalcolata), in circa due secondi
 * invece di centosessanta. Ogni thread
 * parte da una chiave diversa (t % 10), cosi' i dieci calcoli avvengono in parallelo;
 * se tutti chiedessero le chiavi nello stesso ordine, i calcoli sarebbero uno alla
 * volta (dieci secondi), perche' tutti aspetterebbero lo stesso Future.
 */
public class MemoApp {

    public static void main(String[] args) {
        AtomicInteger chiamate = new AtomicInteger();
        AtomicBoolean setteGiaFallita = new AtomicBoolean();
        Memo<Integer, String> memo = new Memo<>(k -> {
            chiamate.incrementAndGet();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (k == 7 && setteGiaFallita.compareAndSet(false, true))
                throw new IllegalArgumentException("la chiave 7 fallisce la prima volta");
            return "valore di " + k;
        });

        long t0 = System.nanoTime();
        try (ExecutorService executor = Executors.newFixedThreadPool(16)) {
            for (int t = 0; t < 16; t++) {
                int inizio = t % 10;
                executor.execute(() -> {
                    for (int i = 0; i < 10; i++) {
                        int k = (inizio + i) % 10;
                        try { memo.apply(k); } catch (RuntimeException e) { /* la 7, la prima volta */ }
                    }
                });
            }
        }
        System.out.printf("160 richieste, %d chiamate alla funzione, in %d ms%n", chiamate.get(), (System.nanoTime() - t0) / 1_000_000);
        System.out.println("la chiave 7 adesso: " + memo.apply(7) + " (ricalcolata, non avvelenata)");
        System.out.println("chiamate totali: " + chiamate.get());
    }
}
