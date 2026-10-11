package it.unibs.pajc.redolfi.e07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Esercizio 6.7: un pool di connessioni.
 *
 * Venti compiti su un pool di otto thread aprono una connessione (tre permessi),
 * lavorano fra 100 e 500 ms, chiudono. Un AtomicInteger conta quanti sono dentro e ne
 * registra il massimo: non supera mai tre. Con l'argomento "timeout" i compiti usano
 * tryAcquire con 300 ms e chi non entra rinuncia.
 *
 * Errore tipico: chiudi() nel finally senza sapere se apri() era riuscita: con il
 * timeout, un compito che ha rinunciato "restituirebbe" un permesso mai preso e il
 * servizio ammetterebbe quattro connessioni. Qui chiudi() sta nel finally *dentro* il
 * ramo in cui l'apertura e' riuscita.
 */
public class PoolDiConnessioni {

    public static void main(String[] args) throws InterruptedException {
        boolean conTimeout = args.length > 0 && args[0].equals("timeout");
        Connessioni connessioni = new Connessioni(3);
        AtomicInteger dentro = new AtomicInteger(), massimo = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            for (int i = 1; i <= 20; i++) {
                int n = i;
                executor.execute(() -> {
                    try {
                        boolean aperta = conTimeout ? connessioni.apri(300) : apriSempre(connessioni);
                        if (!aperta) {
                            System.out.printf("compito %2d: aspettato 300 ms, rinuncio%n", n);
                            return;
                        }
                        try {
                            int adesso = dentro.incrementAndGet();
                            massimo.accumulateAndGet(adesso, Math::max);
                            System.out.printf("compito %2d: dentro (%d dentro, %d permessi liberi)%n", n, adesso, connessioni.disponibili());
                            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 501));
                            dentro.decrementAndGet();
                            System.out.printf("compito %2d: esco%n", n);
                        } finally {
                            connessioni.chiudi();             // solo se apri() e' riuscita
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        System.out.println("massimo contemporaneamente dentro: " + massimo.get());
    }

    static boolean apriSempre(Connessioni c) throws InterruptedException { c.apri(); return true; }
}
