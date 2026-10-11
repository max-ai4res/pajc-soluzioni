package it.unibs.pajc.redolfi.e09;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Esercizio 5.9: produttore e consumatore con il Buffer a monitor.
 *
 * Il produttore mette 1..1000 e poi una SENTINELLA (-1), un valore che non puo' essere
 * un dato: e' il segnale di fine. Il consumatore somma finche' non la trova.
 *
 * Con due consumatori (argomento "2") il segnale deve raggiungerli entrambi: chi trova
 * la sentinella la *rimette* nel buffer per l'altro e poi esce; in alternativa il
 * produttore ne mette una per consumatore. Le somme parziali dei due consumatori si
 * uniscono alla fine: qui con un AtomicLong (capitolo 6), oppure ciascuno in un campo
 * letto dopo il join.
 *
 * Errore tipico: chiudere il consumatore "quando il buffer e' vuoto": vuoto non vuol
 * dire finito, il produttore puo' essere solo in ritardo.
 */
public class ProduttoreConsumatore {

    static final int SENTINELLA = -1;

    public static void main(String[] args) throws InterruptedException {
        int nConsumatori = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        Buffer<Integer> buffer = new Buffer<>(10);
        AtomicLong totale = new AtomicLong();

        Thread produttore = new Thread(() -> {
            try {
                for (int i = 1; i <= 1000; i++) buffer.metti(i);
                buffer.metti(SENTINELLA);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "produttore");

        Thread[] consumatori = new Thread[nConsumatori];
        for (int c = 0; c < nConsumatori; c++) {
            consumatori[c] = new Thread(() -> {
                try {
                    long somma = 0;
                    for (int v; (v = buffer.prendi()) != SENTINELLA; ) somma += v;
                    buffer.metti(SENTINELLA);        // la rimette per gli altri consumatori
                    totale.addAndGet(somma);
                    System.out.println(Thread.currentThread().getName() + ": somma parziale " + somma);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "consumatore-" + c);
        }

        produttore.start();
        for (Thread t : consumatori) t.start();
        produttore.join();
        for (Thread t : consumatori) t.join();
        System.out.println("totale: " + totale.get() + " (atteso 500500)");
    }
}
