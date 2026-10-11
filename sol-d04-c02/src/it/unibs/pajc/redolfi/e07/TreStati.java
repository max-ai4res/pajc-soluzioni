package it.unibs.pajc.redolfi.e07;

/**
 * Esercizio 2.7: tre stati in una fotografia.
 *
 * Idea: tre thread, ciascuno portato in uno stato diverso.
 *  - `dormiente` chiama sleep(60 s)                        -> TIMED_WAITING
 *  - `inAttesa` fa join() su `dormiente`, senza scadenza    -> WAITING
 *  - `bloccato` prova a entrare in synchronized (lock) mentre main tiene il monitor
 *                                                           -> BLOCKED
 * Il main entra in synchronized (lock) e, *dentro* al blocco, avvia il terzo thread e
 * aspetta un secondo: finche' main non esce dal blocco, `bloccato` resta fermo sulla
 * porta del monitor. Dopo un secondo main stampa getState() dei tre e termina la JVM
 * con System.exit (i tre thread non sono daemon e altrimenti la terrebbero viva).
 *
 * Con `jstack <pid>` durante il secondo di attesa si vede la stessa fotografia:
 * "waiting on condition" / TIMED_WAITING, WAITING (on object monitor), e
 * "waiting for monitor entry" / BLOCKED con la riga "waiting to lock <...>".
 *
 * Errore tipico: avviare `bloccato` *prima* di entrare nel blocco synchronized: il
 * thread entra per primo e resta lui dentro, e sarebbe main a bloccarsi.
 */
public class TreStati {

    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread dormiente = new Thread(() -> pausa(60_000), "dormiente");
        Thread inAttesa = new Thread(() -> {
            try {
                dormiente.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "inAttesa");
        Thread bloccato = new Thread(() -> {
            synchronized (lock) {
                System.out.println("bloccato: entrato (solo dopo la fotografia, quando main esce dal blocco)");
            }
        }, "bloccato");

        dormiente.start();
        inAttesa.start();
        synchronized (lock) {           // main tiene il monitor...
            bloccato.start();           // ...e solo ora avvia chi lo vuole
            Thread.sleep(1000);
            System.out.println("pid " + ProcessHandle.current().pid() + ", fotografia dopo un secondo:");
            for (Thread t : new Thread[] { dormiente, inAttesa, bloccato })
                System.out.printf("  %-10s %s%n", t.getName(), t.getState());
        }
        System.exit(0);
    }

    static void pausa(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
