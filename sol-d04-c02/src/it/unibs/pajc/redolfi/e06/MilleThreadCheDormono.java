package it.unibs.pajc.redolfi.e06;

import java.util.ArrayList;
import java.util.List;

/**
 * Esercizio 2.6: mille thread che dormono.
 *
 * Idea: creiamo N thread che dormono dieci secondi e misuriamo con System.nanoTime()
 * il tempo fra il primo e l'ultimo start(). Ogni start() chiede al sistema operativo un
 * thread del kernel (pthread_create sotto): costa qualche decina di microsecondi, e il
 * tempo totale cresce in modo circa lineare con N. Mentre dormono, `jstack <pid>` li
 * mostra tutti in TIMED_WAITING e il monitor di sistema mostra memoria *virtuale*
 * riservata (uno stack per thread) ma poca memoria reale: uno stack si consuma solo
 * quando lo si usa.
 *
 * Eseguire con N = 100, 1000, 10000:  java ... MilleThreadCheDormono 10000
 * Con N molto grande (decine di migliaia) il sistema puo' rifiutare la creazione
 * (OutOfMemoryError: unable to create native thread): e' il limite dei thread di
 * piattaforma che i thread virtuali del capitolo 7 superano.
 *
 * Errore tipico: misurare il tempo includendo i join(), cioe' i dieci secondi di
 * sonno: il tempo di creazione va preso subito dopo l'ultimo start().
 */
public class MilleThreadCheDormono {

    public static void main(String[] args) throws InterruptedException {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 1000;
        List<Thread> thread = new ArrayList<>();
        Runnable dormi = () -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(dormi, "dormiente-" + i);
            t.start();
            thread.add(t);
        }
        long creazione = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("%d thread avviati in %d ms (%.1f us per thread)%n",
                n, creazione, creazione * 1000.0 / n);
        System.out.println("dormono: e' il momento di guardarli con jstack " + ProcessHandle.current().pid());

        for (Thread t : thread) t.join();
        System.out.println("tutti svegli, fine");
    }
}
