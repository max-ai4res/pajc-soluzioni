package it.unibs.pajc.redolfi.e03;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * Palestra 9.3: il crivello a blocchi (segmentato) fino a 10^9.
 *
 * 1. I primi fino a sqrt(10^9) (31 623) con un crivello classico, una volta sola, in
 *    una lista condivisa in sola lettura (immutabile dopo la costruzione: nessun lock).
 * 2. L'intervallo [2, 10^9] in blocchi di un milione; ogni blocco e' un compito: crea
 *    un boolean[] LOCALE (confinato), segna i multipli di ogni primo piccolo a partire
 *    dal primo multiplo nel blocco, conta i sopravvissuti e fa trovati.add(n).
 * Il LongAdder e' l'unico stato condiviso scritto da piu' thread.
 *
 * Risultato atteso: 50 847 534. Tempo tipico su 16 core: ~1 s (contro ~10 s a un
 * thread, e minuti per divisioni successive fino a 10^8).
 *
 * Errore tipico: un unico array condiviso di 10^9 boolean (un gigabyte, e scritto da
 * tutti i thread insieme). L'array e' del compito: un megabyte per blocco.
 */
public class CrivelloABlocchi {

    static final long MAX = 1_000_000_000L;
    static final int BLOCCO = 1_000_000;

    static List<Integer> primiPiccoli(int fino) {
        boolean[] composto = new boolean[fino + 1];
        List<Integer> primi = new ArrayList<>();
        for (int p = 2; p <= fino; p++) {
            if (composto[p]) continue;
            primi.add(p);
            for (long m = (long) p * p; m <= fino; m += p) composto[(int) m] = true;
        }
        return List.copyOf(primi);                           // immutabile: condivisa in lettura
    }

    static long crivellaBlocco(long da, long a, List<Integer> primi) {
        boolean[] composto = new boolean[(int) (a - da + 1)]; // locale al compito
        for (int p : primi) {
            long primoMultiplo = Math.max((long) p * p, (da + p - 1) / p * p);
            for (long m = primoMultiplo; m <= a; m += p) composto[(int) (m - da)] = true;
        }
        long n = 0;
        for (int i = 0; i < composto.length; i++)
            if (!composto[i] && da + i >= 2) n++;
        return n;
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> primi = primiPiccoli((int) Math.sqrt(MAX) + 1);
        for (int nThread : new int[] { 1, Runtime.getRuntime().availableProcessors() }) {
            long t0 = System.nanoTime();
            LongAdder trovati = new LongAdder();
            try (ExecutorService executor = Executors.newFixedThreadPool(nThread)) {
                for (long da = 0; da <= MAX; da += BLOCCO) {
                    long inizio = da, fine = Math.min(MAX, da + BLOCCO - 1);
                    executor.execute(() -> trovati.add(crivellaBlocco(inizio, fine, primi)));
                }
            }
            System.out.printf("%2d thread: %d primi fino a %d in %d ms%n", nThread, trovati.sum(), MAX,
                    (System.nanoTime() - t0) / 1_000_000);
        }
    }
}
