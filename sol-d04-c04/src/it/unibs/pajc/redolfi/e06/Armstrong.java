package it.unibs.pajc.redolfi.e06;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Esercizio 4.6: numeri di Armstrong fino a 10^8, su tutti i core.
 *
 * Idea: trova(da, a) e' un metodo puro, senza stato condiviso. Il main divide
 * l'intervallo in 32 compiti Callable<List<Long>>, li consegna a un pool con tanti
 * thread quanti i core e raccoglie i Future *nell'ordine di consegna*: cosi' i risultati
 * escono gia' in ordine crescente senza ordinarli, e nessuna lista e' condivisa fra
 * thread (ogni compito restituisce la propria). Il try con risorse spegne il pool.
 *
 * Misura tipica su 16 core: 1 thread ~6 s, pool ~0,7 s. Il guadagno non e' 16x: il
 * lavoro per numero cresce con il numero di cifre, gli ultimi compiti costano di piu'
 * e la somma finale e' sequenziale (Amdahl).
 *
 * Errore tipico: far aggiungere i risultati a una ArrayList condivisa dentro i compiti:
 * ordine casuale e, con piu' thread che scrivono insieme, una lista corrotta
 * (capitolo 5).
 */
public class Armstrong {

    public static boolean isArmstrong(long n) {
        int cifre = String.valueOf(n).length();
        long somma = 0, resto = n;
        while (resto > 0) {
            somma += (long) Math.pow(resto % 10, cifre);
            resto /= 10;
        }
        return somma == n;
    }

    public static List<Long> trova(long da, long a) {
        List<Long> trovati = new ArrayList<>();
        for (long n = da; n <= a; n++)
            if (isArmstrong(n)) trovati.add(n);
        return trovati;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        long max = 100_000_000L;
        int nTask = 32;
        int nThread = Runtime.getRuntime().availableProcessors();

        long t0 = System.nanoTime();
        List<Long> sequenziale = trova(1, max);
        System.out.printf("1 thread:  %s in %d ms%n", sequenziale, (System.nanoTime() - t0) / 1_000_000);

        t0 = System.nanoTime();
        List<Long> trovati = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(nThread)) {
            List<Future<List<Long>>> risultati = new ArrayList<>();
            long perTask = max / nTask;
            for (int i = 0; i < nTask; i++) {
                long da = i * perTask + 1;
                long a = (i == nTask - 1) ? max : (i + 1) * perTask;
                risultati.add(executor.submit(() -> trova(da, a)));
            }
            for (Future<List<Long>> f : risultati)
                trovati.addAll(f.get());            // nell'ordine degli intervalli
        }
        System.out.printf("%d thread: %s in %d ms%n", nThread, trovati, (System.nanoTime() - t0) / 1_000_000);
    }
}
