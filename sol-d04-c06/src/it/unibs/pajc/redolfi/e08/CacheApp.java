package it.unibs.pajc.redolfi.e08;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * Esercizio 6.8: quindici lettori e uno scrittore per tre secondi, nelle tre varianti.
 *
 * Risultato tipico su 16 core: il monitor serializza anche i lettori fra loro e fa il
 * numero piu' basso di get; il ReadWriteLock lascia leggere insieme ma ogni get paga
 * l'acquisizione (un compare-and-swap su un contatore condiviso), e con letture cosi'
 * brevi il guadagno e' modesto o nullo; ConcurrentHashMap legge senza lock e vince di
 * molto. La lezione: ReadWriteLock conviene con letture *lunghe* e rare scritture, non
 * con un get di una mappa.
 */
public class CacheApp {

    public static void main(String[] args) throws InterruptedException {
        for (String modo : new String[] { "monitor", "rw", "concurrent" }) {
            Cache<Integer, String> cache = new Cache<>(modo);
            for (int i = 0; i < 1000; i++) cache.put(i, "v" + i);
            LongAdder letture = new LongAdder();
            long fine = System.nanoTime() + 3_000_000_000L;

            try (ExecutorService executor = Executors.newFixedThreadPool(16)) {
                for (int r = 0; r < 15; r++)
                    executor.execute(() -> {
                        int k = 0;
                        while (System.nanoTime() < fine) {
                            cache.get(k++ % 1000);
                            letture.increment();
                        }
                    });
                executor.execute(() -> {
                    int k = 0;
                    while (System.nanoTime() < fine) {
                        cache.put(k++ % 1000, "w" + k);
                        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                    }
                });
            }
            System.out.printf("%-11s %,d get in 3 s%n", modo, letture.sum());
        }
    }
}
