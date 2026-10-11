package it.unibs.pajc.redolfi.e08;

/**
 * Esercizio 5.8: il deadlock dal vivo.
 *
 * Con l'argomento "deadlock" usa trasferisciMale: due thread trasferiscono in
 * continuazione x->y e y->x, prendendo i monitor in ordine opposto; il blocco arriva in
 * una frazione di secondo. jstack <pid> stampa in fondo:
 *
 *   Found one Java-level deadlock:
 *   "x->y": waiting to lock monitor 0x... (object 0x..., a Conto),
 *     which is held by "y->x"
 *   "y->x": waiting to lock monitor 0x... (object 0x..., a Conto),
 *     which is held by "x->y"
 *
 * Senza argomento usa trasferisciBene: i due monitor si prendono sempre in ordine di
 * id crescente, un ciclo di attese non puo' formarsi, e il revisore (qui main) legge ogni
 * secondo la somma dei saldi tenendo entrambi i monitor: resta 2000. Nella versione con
 * deadlock il revisore non prende i monitor (si bloccherebbe anche lui) e stampa solo gli
 * stati dei due thread: BLOCKED e BLOCKED.
 *
 * Errore tipico: leggere la somma senza i monitor: si puo' cogliere un trasferimento a
 * meta' (prelevato ma non ancora versato) e vedere 1990.
 */
public class DeadlockDalVivo {

    static void trasferisciMale(Conto da, Conto a, int importo) {
        synchronized (da) {
            synchronized (a) {
                da.preleva(importo);
                a.versa(importo);
            }
        }
    }

    static void trasferisciBene(Conto da, Conto a, int importo) {
        Conto primo = da.id() < a.id() ? da : a;
        Conto secondo = da.id() < a.id() ? a : da;
        synchronized (primo) {
            synchronized (secondo) {
                da.preleva(importo);
                a.versa(importo);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        boolean male = args.length > 0 && args[0].equals("deadlock");
        Conto x = new Conto(1, 1000), y = new Conto(2, 1000);
        System.out.println("pid " + ProcessHandle.current().pid() + (male ? " (versione con deadlock)" : ""));

        Thread t1 = new Thread(() -> { while (true) if (male) trasferisciMale(x, y, 10); else trasferisciBene(x, y, 10); }, "x->y");
        Thread t2 = new Thread(() -> { while (true) if (male) trasferisciMale(y, x, 20); else trasferisciBene(y, x, 20); }, "y->x");
        t1.setDaemon(true); t2.setDaemon(true);
        t1.start(); t2.start();

        for (int s = 1; s <= 5; s++) {
            Thread.sleep(1000);
            if (male) {
                // nella versione con deadlock anche il revisore si bloccherebbe prendendo i
                // monitor: si limita a guardare gli stati (BLOCKED / BLOCKED) e a rimandare a jstack
                System.out.printf("dopo %d s: stati %s / %s  (jstack %d per il ciclo)%n",
                        s, t1.getState(), t2.getState(), ProcessHandle.current().pid());
            } else {
                int somma;
                synchronized (x) { synchronized (y) { somma = x.saldo() + y.saldo(); } }
                System.out.printf("dopo %d s: somma = %d, stati %s / %s%n", s, somma, t1.getState(), t2.getState());
            }
        }
    }
}
