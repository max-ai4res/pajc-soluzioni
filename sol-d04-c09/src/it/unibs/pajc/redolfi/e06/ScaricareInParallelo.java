package it.unibs.pajc.redolfi.e06;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Palestra 9.6: scaricare in parallelo.
 *
 * Cinquanta "file" di 1-20 MB; scaricare un file = dormire 1 ms ogni 10 KB (cioe'
 * aspettare: nessun calcolo), aggiornando un AtomicLong a blocchi di 100 KB. Un thread
 * virtuale per file: tutte le attese insieme, tempo totale = quello del file piu'
 * grande (~2 s). Con un pool fisso di quattro thread si scaricano quattro file alla
 * volta: ~ (somma delle attese) / 4, cioe' decine di secondi.
 *
 * Il thread di piattaforma "barra" legge l'AtomicLong ogni 200 ms e ridisegna la barra
 * sulla stessa riga (\r). Il valore letto e' coerente ma gia' vecchio di qualche
 * microsecondo: per una barra e' esattamente cio' che serve.
 */
public class ScaricareInParallelo {

    static final int N_FILE = 50;

    public static void main(String[] args) throws InterruptedException {
        long[] dimensioni = new long[N_FILE];
        long totaleAtteso = 0;
        for (int i = 0; i < N_FILE; i++) {
            dimensioni[i] = ThreadLocalRandom.current().nextLong(1, 21) * 1_000_000L;
            totaleAtteso += dimensioni[i];
        }
        scarica("un thread virtuale per file", Executors.newVirtualThreadPerTaskExecutor(), dimensioni, totaleAtteso);
        scarica("pool fisso di 4", Executors.newFixedThreadPool(4), dimensioni, totaleAtteso);
    }

    static void scarica(String nome, ExecutorService executor, long[] dimensioni, long totaleAtteso) throws InterruptedException {
        AtomicLong ricevuti = new AtomicLong();
        long t0 = System.nanoTime();
        Thread barra = new Thread(() -> {
            try {
                while (true) {
                    stampaBarra(ricevuti.get(), totaleAtteso);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) { /* fine: l'ultima barra la stampa main */ }
        }, "barra");
        barra.start();

        try (executor) {
            for (long dimensione : dimensioni)
                executor.execute(() -> {
                    for (long fatti = 0; fatti < dimensione; fatti += 100_000) {
                        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                        ricevuti.addAndGet(Math.min(100_000, dimensione - fatti));
                    }
                });
        }
        barra.interrupt();
        barra.join();
        stampaBarra(ricevuti.get(), totaleAtteso);
        System.out.printf("%n%s: %d MB in %d ms%n", nome, totaleAtteso / 1_000_000, (System.nanoTime() - t0) / 1_000_000);
    }

    static void stampaBarra(long fatti, long totale) {
        int percento = (int) (100 * fatti / totale);
        int pieni = percento / 5;
        System.out.print("\r[" + "#".repeat(pieni) + " ".repeat(20 - pieni) + "] " + percento + "%");
        System.out.flush();
    }
}
