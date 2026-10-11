package it.unibs.pajc.redolfi.e08;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.DoubleUnaryOperator;

/**
 * Esercizio 4.8: Matrix.map e Matrix.mapMT.
 *
 * Idea: map applica f a ogni elemento in un doppio ciclo. mapMT divide le *righe* fra
 * nThread compiti: ogni compito scrive solo le proprie righe della matrice risultato,
 * quindi non c'e' stato condiviso da proteggere; invokeAll ritorna quando tutti hanno
 * finito, e il risultato e' completo (e visibile: invokeAll da' la stessa garanzia del
 * join). I compiti sono Callable<Void> che restituiscono null.
 *
 * Quando conviene: con una funzione costosa (sin*exp) il guadagno e' vicino al numero di
 * core veloci; con x -> x + 1 il lavoro per elemento e' una somma, e il tempo e'
 * dominato dalla memoria e dall'avvio dei compiti: mapMT puo' essere piu' lento di map.
 * Il parallelismo paga quando il lavoro per elemento e' grande rispetto al costo di
 * distribuirlo.
 *
 * Errore tipico: dividere per colonne invece che per righe (accessi sparsi, cache
 * fredda), o far creare il pool dentro mapMT a ogni chiamata senza chiuderlo.
 */
public class Matrix {
    private final double[][] a;

    public Matrix(int righe, int colonne, Random random) {
        a = new double[righe][colonne];
        for (double[] riga : a)
            for (int j = 0; j < colonne; j++)
                riga[j] = random.nextDouble();
    }

    private Matrix(double[][] a) { this.a = a; }

    public int righe() { return a.length; }
    public int colonne() { return a[0].length; }
    public double get(int i, int j) { return a[i][j]; }

    public Matrix map(DoubleUnaryOperator f) {
        double[][] r = new double[righe()][colonne()];
        for (int i = 0; i < righe(); i++)
            for (int j = 0; j < colonne(); j++)
                r[i][j] = f.applyAsDouble(a[i][j]);
        return new Matrix(r);
    }

    public Matrix mapMT(DoubleUnaryOperator f, int nThread) throws InterruptedException {
        double[][] r = new double[righe()][colonne()];
        List<Callable<Void>> compiti = new ArrayList<>();
        int perTask = (righe() + nThread - 1) / nThread;
        for (int t = 0; t < nThread; t++) {
            int da = t * perTask;
            int a = Math.min(righe(), da + perTask);
            compiti.add(() -> {
                for (int i = da; i < a; i++)
                    for (int j = 0; j < colonne(); j++)
                        r[i][j] = f.applyAsDouble(this.a[i][j]);
                return null;
            });
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(nThread)) {
            executor.invokeAll(compiti);            // ritorna quando tutti hanno finito
        }
        return new Matrix(r);
    }
}
