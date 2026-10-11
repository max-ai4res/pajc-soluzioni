package it.unibs.pajc.redolfi.e06;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Esercizio 7.6: dieci server lenti.
 *
 * Ogni "server" e' un metodo che dorme fra 200 e 800 ms e risponde. Tre modi di
 * interrogarli tutti: in sequenza (somma dei tempi, ~5 s), pool fisso di quattro thread
 * (~ dieci su quattro: due o tre turni, ~1,5 s), un thread virtuale per server (il tempo
 * del piu' lento, < 1 s). Le risposte si leggono dai Future nell'ordine dei server.
 *
 * Con diecimila server che dormono un secondo (argomento "10000"): in sequenza 10000 s
 * (non si prova), pool di quattro 2500 s (non si prova), thread virtuali poco piu' di un
 * secondo: sono attese, e diecimila attese insieme non costano niente.
 *
 * Errore tipico: usare i thread virtuali *in pool* (newFixedThreadPool con una factory
 * virtuale): si perde l'unico vantaggio, cioe' averne uno per compito.
 */
public class DieciServerLenti {

    static String server(int i, long attesaMs) {
        try {
            Thread.sleep(attesaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "server " + i + " ha risposto dopo " + attesaMs + " ms";
    }

    public static void main(String[] args) throws Exception {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 10;
        long[] attese = new long[n];
        for (int i = 0; i < n; i++)
            attese[i] = n == 10 ? ThreadLocalRandom.current().nextLong(200, 801) : 1000;

        if (n == 10) {
            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) System.out.println(server(i, attese[i]));
            System.out.printf("in sequenza: %d ms%n%n", (System.nanoTime() - t0) / 1_000_000);

            misura("pool fisso di 4", Executors.newFixedThreadPool(4), attese);
        }
        misura("un thread virtuale per server", Executors.newVirtualThreadPerTaskExecutor(), attese);
    }

    static void misura(String nome, ExecutorService executor, long[] attese) throws Exception {
        long t0 = System.nanoTime();
        List<String> risposte = new ArrayList<>();
        try (executor) {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < attese.length; i++) {
                int k = i;
                futures.add(executor.submit(() -> server(k, attese[k])));
            }
            for (Future<String> f : futures) risposte.add(f.get());      // nell'ordine dei server
        }
        if (attese.length <= 10) risposte.forEach(System.out::println);
        System.out.printf("%s: %d ms per %d server%n%n", nome, (System.nanoTime() - t0) / 1_000_000, attese.length);
    }
}
