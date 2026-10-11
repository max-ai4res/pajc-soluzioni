package it.unibs.pajc.redolfi.e05;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Palestra 9.5: code al supermercato.
 *
 * Dati condivisi e come sono protetti:
 *  - la coda dei clienti: BlockingQueue, thread-safe per costruzione (put/take);
 *  - le statistiche delle attese: LongAdder (somma) e LongAccumulator con Math::max
 *    (massimo), aggiornati dalle tre casse senza lock;
 *  - il conteggio per cassa: un AtomicInteger per cassa (o una variabile locale
 *    stampata alla fine: qui locale, perche' la legge solo la sua cassa).
 * Il generatore, dopo cento clienti, mette TRE sentinelle (una per cassa): ogni cassa
 * termina quando trova la sua. Nessun cliente va perso: le casse escono solo dopo la
 * sentinella, che sta in coda dopo l'ultimo cliente.
 */
public class Supermercato {

    record Cliente(int id, long arrivo, int articoli) { }

    static final Cliente SENTINELLA = new Cliente(-1, 0, 0);
    static final int N_CASSE = 3, N_CLIENTI = 100;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Cliente> coda = new ArrayBlockingQueue<>(20);
        LongAdder attesaTotale = new LongAdder();
        LongAccumulator attesaMassima = new LongAccumulator(Math::max, 0);
        AtomicInteger serviti = new AtomicInteger();

        Thread generatore = new Thread(() -> {
            try {
                for (int i = 1; i <= N_CLIENTI; i++) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 40));
                    coda.put(new Cliente(i, System.nanoTime(), ThreadLocalRandom.current().nextInt(1, 30)));
                }
                for (int c = 0; c < N_CASSE; c++) coda.put(SENTINELLA);     // una per cassa
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "generatore");

        Thread[] casse = new Thread[N_CASSE];
        for (int c = 0; c < N_CASSE; c++) {
            casse[c] = new Thread(() -> {
                int miei = 0;                                                  // locale alla cassa
                try {
                    Cliente cliente;
                    while ((cliente = coda.take()) != SENTINELLA) {
                        long attesa = (System.nanoTime() - cliente.arrivo()) / 1_000_000;
                        attesaTotale.add(attesa);
                        attesaMassima.accumulate(attesa);
                        Thread.sleep(cliente.articoli() * 3L);                 // servizio
                        miei++;
                        serviti.incrementAndGet();
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.printf("%s: %d clienti serviti%n", Thread.currentThread().getName(), miei);
            }, "cassa-" + (c + 1));
        }

        generatore.start();
        for (Thread t : casse) t.start();
        generatore.join();
        for (Thread t : casse) t.join();
        System.out.printf("serviti %d su %d; attesa media %d ms, massima %d ms%n",
                serviti.get(), N_CLIENTI, attesaTotale.sum() / N_CLIENTI, attesaMassima.get());
    }
}
