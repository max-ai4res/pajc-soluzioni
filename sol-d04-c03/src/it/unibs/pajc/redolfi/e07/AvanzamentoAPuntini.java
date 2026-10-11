package it.unibs.pajc.redolfi.e07;

/**
 * Esercizio 3.7: avanzamento a puntini.
 *
 * Idea: il lavoro lungo gira su un thread; main, invece di un join() secco, usa
 * join(250): aspetta al massimo 250 ms e poi ritorna comunque. A ogni ritorno main
 * stampa un punto e controlla isAlive(): finche' il thread e' vivo, ricomincia. Quando
 * join ritorna perche' il thread e' finito, isAlive() e' false, main va a capo e legge
 * il risultato dal campo del compito.
 *
 * join(ms) e' preferibile a "sleep(250) + isAlive()" perche' appena il lavoro finisce
 * main si sveglia subito, senza aspettare la fine del quarto di secondo; e perche' dopo
 * un join (anche con timeout, se ritorna per fine del thread) la lettura del risultato
 * e' garantita visibile. Con sleep + isAlive la fine del thread e' vista con ritardo e
 * servirebbe comunque un join finale per la garanzia di visibilita'.
 *
 * Errore tipico: leggere il risultato subito dopo l'ultimo punto, senza che un join
 * sia ritornato per fine del thread: si legge zero, o un valore a meta'.
 */
public class AvanzamentoAPuntini {

    static class Lavoro implements Runnable {
        private long result;

        public void run() { result = PrimeTester.count(1, 30_000_000); }
        public long result() { return result; }
    }

    public static void main(String[] args) throws InterruptedException {
        Lavoro lavoro = new Lavoro();
        Thread t = new Thread(lavoro, "lavoro");
        t.start();

        System.out.print("conto ");
        do {
            t.join(250);                        // al massimo 250 ms, poi ritorna
            System.out.print(".");
            System.out.flush();
        } while (t.isAlive());
        System.out.println();
        System.out.println("primi fino a 30 milioni: " + lavoro.result());
    }
}
