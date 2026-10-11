package it.unibs.pajc.redolfi.e01;

/**
 * Palestra 9.1: contare i thread.
 *
 * (a) due thread nuovi (i due start); r.run() e' eseguito da main. Tre "ciao".
 * (b) t.run() e' eseguito da main (nessun thread); t.start() crea un thread. Due "ciao",
 *     il primo da main.
 * (c) un thread; dopo il join il secondo start() lancia IllegalThreadStateException:
 *     un Thread si avvia una volta sola, anche se e' terminato.
 * (d) nessun thread: tre run() da main. Tre "ciao da main".
 *
 * Thread.activeCount() conta i thread vivi del gruppo: subito dopo uno start() puo'
 * includere il thread appena avviato (se non ha ancora finito), per questo il
 * programma fa una breve pausa prima di contare e stampa il nome di chi ha stampato.
 */
public class ContareIThread {

    public static void main(String[] args) throws InterruptedException {
        Runnable r = () -> System.out.println("ciao da " + Thread.currentThread().getName());

        System.out.println("-- (a)");
        esegui(() -> { new Thread(r).start(); new Thread(r).start(); r.run(); });

        System.out.println("-- (b)");
        esegui(() -> { Thread t = new Thread(r); t.run(); t.start(); });

        System.out.println("-- (c)");
        esegui(() -> { Thread u = new Thread(r); u.start(); u.join(); u.start(); });

        System.out.println("-- (d)");
        esegui(() -> { for (int i = 0; i < 3; i++) new Thread(r).run(); });
    }

    interface Frammento { void run() throws Exception; }

    static void esegui(Frammento f) throws InterruptedException {
        int prima = Thread.activeCount();
        try {
            f.run();
        } catch (Exception e) {
            System.out.println("eccezione: " + e);
        }
        Thread.sleep(50);                       // lascia finire i thread appena avviati
        System.out.println("thread vivi: " + prima + " prima, " + Thread.activeCount() + " dopo");
    }
}
