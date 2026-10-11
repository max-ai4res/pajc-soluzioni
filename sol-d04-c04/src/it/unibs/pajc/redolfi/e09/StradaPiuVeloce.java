package it.unibs.pajc.redolfi.e09;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 4.9: la strada piu' veloce (invokeAny).
 *
 * Tre Callable<Long> contano i primi fino a 5*10^7 in tre modi: divisioni successive,
 * crivello di Eratostene, crivello sui soli dispari. invokeAny restituisce il risultato
 * del primo che finisce e chiama cancel(true) sugli altri. Vince il crivello sui dispari
 * (meta' memoria, meta' lavoro), poi il crivello pieno; le divisioni successive arrivano
 * ultime di molto.
 *
 * Che cosa succede agli altri: cancel(true) manda interrupt() al thread che li esegue,
 * ma un crivello e' un ciclo di puro calcolo che non controlla mai la bandiera: continua
 * fino in fondo (e stampa il suo tempo) anche se nessuno aspetta piu' il risultato. Per
 * farlo smettere bisognerebbe controllare Thread.currentThread().isInterrupted() nel
 * ciclo esterno, come fa qui la versione "collaborativa" delle divisioni successive. Il
 * try con risorse aspetta comunque che tutti finiscano prima di chiudere.
 *
 * Errore tipico: credere che cancel(true) "uccida" il compito: e' una richiesta, e un
 * ciclo che non la guarda non si ferma.
 */
public class StradaPiuVeloce {

    static final long MAX = 50_000_000L;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<Callable<Long>> strategie = List.of(
                StradaPiuVeloce::divisioni,
                StradaPiuVeloce::crivello,
                StradaPiuVeloce::crivelloDispari);
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            long t0 = System.nanoTime();
            long n = executor.invokeAny(strategie);
            System.out.printf("primi fino a %d: %d (il primo ha risposto dopo %d ms)%n",
                    MAX, n, (System.nanoTime() - t0) / 1_000_000);
        }
    }

    static long divisioni() {
        long t0 = System.nanoTime(), n = 0;
        for (long i = 2; i <= MAX; i++) {
            if ((i & 0xFFFF) == 0 && Thread.currentThread().isInterrupted()) {
                System.out.println("divisioni: annullato, smetto");
                return -1;
            }
            if (isPrime(i)) n++;
        }
        stampa("divisioni", n, t0);
        return n;
    }

    static long crivello() {
        long t0 = System.nanoTime();
        boolean[] composto = new boolean[(int) MAX + 1];
        for (long p = 2; p * p <= MAX; p++)
            if (!composto[(int) p])
                for (long m = p * p; m <= MAX; m += p) composto[(int) m] = true;
        long n = 0;
        for (int i = 2; i <= MAX; i++) if (!composto[i]) n++;
        stampa("crivello", n, t0);
        return n;
    }

    static long crivelloDispari() {
        long t0 = System.nanoTime();
        int size = (int) (MAX / 2);                 // indice k rappresenta 2k + 1
        boolean[] composto = new boolean[size + 1];
        for (long p = 3; p * p <= MAX; p += 2)
            if (!composto[(int) (p / 2)])
                for (long m = p * p; m <= MAX; m += 2 * p) composto[(int) (m / 2)] = true;
        long n = 1;                                 // il 2
        for (int k = 1; k <= size && 2L * k + 1 <= MAX; k++) if (!composto[k]) n++;
        stampa("crivello dispari", n, t0);
        return n;
    }

    static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (long d = 3; d * d <= n; d += 2)
            if (n % d == 0) return false;
        return true;
    }

    static void stampa(String nome, long n, long t0) {
        System.out.printf("  %-16s %d in %d ms%n", nome, n, (System.nanoTime() - t0) / 1_000_000);
    }
}
