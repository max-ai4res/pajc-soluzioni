package it.unibs.pajc.redolfi.e09;

/**
 * Esercizio 3.9: quattro thread a mano.
 *
 * Idea: quattro PrimeCounter, ciascuno con il suo intervallo, in un array; un ciclo per
 * gli start e uno per i join (se stessero nello stesso ciclo, ogni thread girerebbe da
 * solo, e il tempo tornerebbe quello sequenziale). Ogni compito misura il proprio tempo.
 *
 * Con intervalli di uguale ampiezza il quarto thread finisce molto dopo il primo: i
 * numeri grandi costano piu' divisioni. Per bilanciare si usa il costo cumulato fino a
 * n, circa n*sqrt(n) = n^1.5: si cercano gli estremi x_k con x_k^1.5 = k/4 * max^1.5,
 * cioe' x_k = max * (k/4)^(2/3). Con max = 10^8: 39.7, 63.0, 82.5, 100 milioni. I
 * quattro tempi diventano simili e il tempo totale (quello del piu' lento) cala.
 *
 * Errore tipico: dividere in parti uguali e concludere che "quattro thread fanno 2x
 * invece di 4x perche' Java e' lento": e' il bilanciamento, non il linguaggio. Il
 * capitolo 4 risolve il problema con molti compiti piccoli e una coda.
 */
public class QuattroThreadAMano {

    static final long MAX = 100_000_000L;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("-- intervalli uguali");
        long[] uguali = { 1, MAX / 4, MAX / 2, 3 * MAX / 4, MAX };
        esegui(uguali);

        System.out.println("-- intervalli bilanciati (costo ~ n^1.5)");
        long[] bilanciati = new long[5];
        bilanciati[0] = 1;
        for (int k = 1; k <= 4; k++)
            bilanciati[k] = Math.round(MAX * Math.pow(k / 4.0, 2.0 / 3.0));
        esegui(bilanciati);
    }

    static void esegui(long[] estremi) throws InterruptedException {
        PrimeCounter[] compiti = new PrimeCounter[4];
        Thread[] thread = new Thread[4];
        for (int k = 0; k < 4; k++) {
            long from = k == 0 ? estremi[0] : estremi[k] + 1;
            compiti[k] = new PrimeCounter(from, estremi[k + 1]);
            thread[k] = new Thread(compiti[k]);
        }
        Chrono totale = new Chrono();
        for (Thread t : thread) t.start();      // tutti partono...
        for (Thread t : thread) t.join();       // ...poi si aspettano tutti
        long nPrimi = 0;
        for (PrimeCounter c : compiti) {
            System.out.printf("  %-24s %8d primi in %6d ms%n", c, c.result(), c.millis());
            nPrimi += c.result();
        }
        System.out.printf("  totale: %d primi in %d ms%n", nPrimi, totale.millis());
    }
}
