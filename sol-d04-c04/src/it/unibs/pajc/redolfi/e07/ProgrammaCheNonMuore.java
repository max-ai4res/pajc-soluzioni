package it.unibs.pajc.redolfi.e07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Esercizio 4.7: il programma che non muore.
 *
 * Tre versioni dello stesso main, selezionabili dall'argomento: "appeso", "ricetta",
 * "risorsa".
 *  - appeso: il pool non viene spento; main finisce, i tre thread (non daemon) restano
 *    in WAITING dentro LinkedBlockingQueue.take() (lo mostra jstack: "parking to wait
 *    for ... LinkedBlockingQueue" sotto ThreadPoolExecutor.getTask). La JVM non esce:
 *    va terminata con Ctrl-C.
 *  - ricetta: shutdown + awaitTermination + shutdownNow, come nella dispensa.
 *  - risorsa: try con risorse (Java 19+): close() = shutdown + attesa illimitata.
 *
 * Errore tipico: credere che il programma sia "bloccato su qualcosa di mio": i thread
 * aspettano semplicemente altri compiti, e lo stato WAITING su take() lo dice.
 */
public class ProgrammaCheNonMuore {

    public static void main(String[] args) throws InterruptedException {
        String modo = args.length > 0 ? args[0] : "risorsa";
        switch (modo) {
            case "appeso" -> {
                ExecutorService executor = Executors.newFixedThreadPool(3);
                consegna(executor);
                System.out.println("main finito; pid " + ProcessHandle.current().pid() + ": guardami con jstack");
            }
            case "ricetta" -> {
                ExecutorService executor = Executors.newFixedThreadPool(3);
                consegna(executor);
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS))
                        executor.shutdownNow();
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                System.out.println("main finito, pool spento");
            }
            default -> {
                try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
                    consegna(executor);
                }                                   // close(): shutdown e attesa
                System.out.println("main finito, pool chiuso dal try");
            }
        }
    }

    static void consegna(ExecutorService executor) {
        for (int i = 1; i <= 5; i++) {
            int n = i;
            executor.execute(() -> System.out.println("compito " + n + " su " + Thread.currentThread().getName()));
        }
    }
}
