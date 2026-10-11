package it.unibs.pajc.redolfi.e05;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Esercizio 7.5: periodico e lento.
 *
 * Un compito che stampa l'ora e dorme tre secondi, con periodo un secondo:
 *  - scheduleAtFixedRate:    inizi a 0, 3, 6, 9, 12 s (il periodo e' piu' corto del
 *                            lavoro: ogni esecuzione parte alla prima scadenza utile
 *                            dopo la fine della precedente, senza recuperare i turni);
 *  - scheduleWithFixedDelay: inizi a 0, 4, 8, 12 s (tre di lavoro, uno di pausa).
 *
 * Nella terza fase il compito lancia alla quarta esecuzione: le ripetizioni si fermano
 * in silenzio. Il programma se ne accorge con future.isDone() (true da quel momento) e
 * future.get(), che rilancia ExecutionException con dentro l'eccezione originale. Un
 * get() su un periodico sano non ritornerebbe mai: per questo prima si guarda isDone().
 */
public class PeriodicoELento {

    static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable lento = () -> {
            System.out.println("inizio alle " + LocalTime.now().format(ORA));
            pausa(3000);
        };

        System.out.println("-- scheduleAtFixedRate, periodo 1 s (attesi: 0, 3, 6, 9, 12)");
        ScheduledFuture<?> f1 = executor.scheduleAtFixedRate(lento, 0, 1, TimeUnit.SECONDS);
        Thread.sleep(15_000);
        f1.cancel(true);

        System.out.println("-- scheduleWithFixedDelay, pausa 1 s (attesi: 0, 4, 8, 12)");
        ScheduledFuture<?> f2 = executor.scheduleWithFixedDelay(lento, 0, 1, TimeUnit.SECONDS);
        Thread.sleep(15_000);
        f2.cancel(true);

        System.out.println("-- un compito che muore alla quarta esecuzione");
        AtomicInteger esecuzioni = new AtomicInteger();
        ScheduledFuture<?> f3 = executor.scheduleAtFixedRate(() -> {
            int n = esecuzioni.incrementAndGet();
            System.out.println("esecuzione " + n + " alle " + LocalTime.now().format(ORA));
            if (n == 4) throw new IllegalStateException("qualcosa e' andato storto alla " + n);
        }, 0, 1, TimeUnit.SECONDS);
        for (int s = 1; s <= 7; s++) {
            Thread.sleep(1000);
            if (f3.isDone()) {
                try {
                    f3.get();
                } catch (ExecutionException e) {
                    System.out.println("il periodico e' morto: " + e.getCause());
                }
                break;
            }
        }
        executor.shutdownNow();
    }

    static void pausa(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
