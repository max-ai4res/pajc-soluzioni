package it.unibs.pajc.redolfi.e02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Palestra 9.2: numeri felici, con il metodo multithread nello stile delle prove scritte.
 *
 * contaMT divide [da, a] in nThread intervalli: ampiezza = (a - da + 1) / nThread, il
 * k-esimo va da da + k*ampiezza a da + (k+1)*ampiezza - 1, e l'ULTIMO arriva fino ad a
 * (cosi' niente numeri contati due volte o mai). Ogni intervallo e' un Callable che
 * chiama conta; i Future si sommano. isFelice e' pura: nessuno stato condiviso.
 *
 * Il main verifica conta == contaMT fino a 10^7 e misura con 1, 2, 4, 8 thread.
 */
public class Felici {

    public static boolean isFelice(int n) {
        while (n != 1 && n != 4) {
            int somma = 0;
            for (int m = n; m > 0; m /= 10) somma += (m % 10) * (m % 10);
            n = somma;
        }
        return n == 1;
    }

    public static int conta(int da, int a) {
        int n = 0;
        for (int i = da; i <= a; i++)
            if (isFelice(i)) n++;
        return n;
    }

    public static int contaMT(int da, int a, int nThread) throws Exception {
        List<Callable<Integer>> compiti = new ArrayList<>();
        int ampiezza = (a - da + 1) / nThread;
        for (int k = 0; k < nThread; k++) {
            int inizio = da + k * ampiezza;
            int fine = (k == nThread - 1) ? a : inizio + ampiezza - 1;   // l'ultimo fino ad a
            compiti.add(() -> conta(inizio, fine));
        }
        int totale = 0;
        try (ExecutorService executor = Executors.newFixedThreadPool(nThread)) {
            for (Future<Integer> f : executor.invokeAll(compiti)) totale += f.get();
        }
        return totale;
    }

    public static void main(String[] args) throws Exception {
        int max = 10_000_000;
        long t0 = System.nanoTime();
        int seq = conta(1, max);
        System.out.printf("conta:      %d felici in %d ms%n", seq, (System.nanoTime() - t0) / 1_000_000);
        for (int nThread : new int[] { 1, 2, 4, 8 }) {
            t0 = System.nanoTime();
            int mt = contaMT(1, max, nThread);
            System.out.printf("contaMT(%d): %d felici in %d ms %s%n", nThread, mt,
                    (System.nanoTime() - t0) / 1_000_000, mt == seq ? "ok" : "DIVERSO!");
        }
    }
}
