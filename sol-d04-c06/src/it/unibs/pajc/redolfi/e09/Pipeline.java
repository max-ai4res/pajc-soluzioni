package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Esercizio 6.9: una pipeline a tre stadi su due BlockingQueue.
 *
 *   lettore  --coda1-->  trasformatore  --coda2-->  scrittore
 *
 * Il segnale di fine e' una SENTINELLA (una stringa che non puo' essere una riga: qui un
 * oggetto distinto, confrontato con ==). Il lettore la mette in coda1 dopo l'ultima
 * riga; il trasformatore, quando la riceve, la inoltra in coda2 e termina; lo scrittore,
 * quando la riceve, chiude il file e termina. Ogni stadio, se interrotto, rialza la
 * bandiera ed esce: gli altri stadi non ricevono la sentinella, e main li interrompe
 * tutti dopo i join con timeout.
 *
 * Capienza delle code: con 1 i tre stadi procedono quasi in lockstep (ogni put aspetta
 * il take dell'altro); con 10 o 1000 il cuscinetto assorbe le differenze di velocita'.
 * Su un file piccolo i tempi sono tutti trascurabili; con un file di milioni di righe
 * la capienza 1 si sente, mentre fra 10 e 1000 non cambia quasi nulla: basta un
 * cuscinetto piccolo per disaccoppiare gli stadi.
 *
 * Uso: java ... Pipeline [input] [output] [capienza]   (default res/e09/input.txt)
 */
public class Pipeline {

    static final String SENTINELLA = new String("<FINE>");   // identita' distinta, confronto con ==

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args.length > 0 ? args[0] : "res/e09/input.txt");
        Path output = Path.of(args.length > 1 ? args[1] : "res/e09/output.txt");
        int capienza = args.length > 2 ? Integer.parseInt(args[2]) : 10;

        BlockingQueue<String> coda1 = new ArrayBlockingQueue<>(capienza);
        BlockingQueue<String> coda2 = new ArrayBlockingQueue<>(capienza);

        Thread lettore = new Thread(() -> {
            try (BufferedReader in = Files.newBufferedReader(input)) {
                String riga;
                while ((riga = in.readLine()) != null) coda1.put(riga);
                coda1.put(SENTINELLA);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "lettore");

        Thread trasformatore = new Thread(() -> {
            try {
                long n = 0;
                String riga;
                while ((riga = coda1.take()) != SENTINELLA)
                    coda2.put(String.format("%4d  %s", ++n, riga.toUpperCase()));
                coda2.put(SENTINELLA);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "trasformatore");

        Thread scrittore = new Thread(() -> {
            try (BufferedWriter out = Files.newBufferedWriter(output)) {
                String riga;
                while ((riga = coda2.take()) != SENTINELLA) {
                    out.write(riga);
                    out.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "scrittore");

        long t0 = System.nanoTime();
        lettore.start(); trasformatore.start(); scrittore.start();
        for (Thread t : new Thread[] { lettore, trasformatore, scrittore }) {
            t.join(10_000);
            if (t.isAlive()) { System.out.println(t.getName() + " non ha finito: interrompo tutto"); break; }
        }
        for (Thread t : new Thread[] { lettore, trasformatore, scrittore }) t.interrupt();
        System.out.printf("pipeline con code di capienza %d: %d ms, output in %s%n",
                capienza, (System.nanoTime() - t0) / 1_000_000, output);
    }
}
