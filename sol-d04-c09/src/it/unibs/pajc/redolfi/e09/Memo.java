package it.unibs.pajc.redolfi.e09;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * Palestra 9.9: la cache che calcola una volta sola (il Memoizer di Goetz et al.).
 *
 * La mappa contiene Future<V>, non V: il Future e' il segnaposto che dice "qualcuno sta
 * gia' calcolando questa chiave". computeIfAbsent inserisce il FutureTask in modo
 * atomico ma NON lo esegue (la lambda crea solo l'oggetto: niente calcolo sotto il
 * lock del segmento); chi ha inserito lo esegue subito dopo con run(), fuori dalla
 * mappa; chi trova il Future gia' presente fa get() e aspetta il risultato dell'altro.
 *
 * Se la funzione lancia, il Future contiene l'eccezione: la chiave resta "avvelenata"
 * finche' non la si toglie. Qui, nel catch, si fa mappa.remove(k, f) (solo se e' ancora
 * quel Future) e si rilancia la causa: il prossimo che chiede ricalcola.
 */
public class Memo<K, V> implements Function<K, V> {
    private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<>();
    private final Function<K, V> funzione;

    public Memo(Function<K, V> funzione) { this.funzione = funzione; }

    @Override
    public V apply(K k) {
        Future<V> f = cache.get(k);
        if (f == null) {
            FutureTask<V> nuovo = new FutureTask<>(() -> funzione.apply(k));
            f = cache.computeIfAbsent(k, chiave -> nuovo);   // atomico, istantaneo
            if (f == nuovo) nuovo.run();                       // l'ho inserito io: calcolo io, fuori dal lock
        }
        try {
            return f.get();                                    // gli altri aspettano qui
        } catch (CancellationException | ExecutionException e) {
            cache.remove(k, f);                                // non lasciare la chiave avvelenata
            Throwable causa = e instanceof ExecutionException ? e.getCause() : e;
            if (causa instanceof RuntimeException re) throw re;
            throw new RuntimeException(causa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
