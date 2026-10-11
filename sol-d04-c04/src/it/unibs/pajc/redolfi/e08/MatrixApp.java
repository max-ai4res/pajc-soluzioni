package it.unibs.pajc.redolfi.e08;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/** Esercizio 4.8: misura map e mapMT con una funzione costosa e una banale. */
public class MatrixApp {

    public static void main(String[] args) throws InterruptedException {
        int nThread = Runtime.getRuntime().availableProcessors();
        Matrix m = new Matrix(4000, 4000, new Random(42));

        misura("sin*exp", m, x -> Math.sin(x) * Math.exp(x), nThread);
        misura("x + 1  ", m, x -> x + 1, nThread);
    }

    static void misura(String nome, Matrix m, DoubleUnaryOperator f, int nThread) throws InterruptedException {
        long t0 = System.nanoTime();
        Matrix r1 = m.map(f);
        long tMap = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        Matrix r2 = m.mapMT(f, nThread);
        long tMT = (System.nanoTime() - t0) / 1_000_000;

        boolean uguali = r1.get(123, 456) == r2.get(123, 456) && r1.get(3999, 3999) == r2.get(3999, 3999);
        System.out.printf("%s  map: %5d ms   mapMT(%d): %5d ms   stesso risultato: %b%n",
                nome, tMap, nThread, tMT, uguali);
    }
}
