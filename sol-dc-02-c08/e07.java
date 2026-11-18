import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * Esercizio 8.7 - Il leak, e la cura
 *
 * Un "servizio" che per ogni richiesta calcola un risultato di un megabyte.
 * Tre versioni, provate una dopo l'altra nello stesso heap:
 *
 *     java -Xmx32m e07.java
 *
 *  1. con una cache statica senza limite: ogni risultato resta raggiungibile
 *     da una radice (il campo static), il garbage collector non puo'
 *     recuperarlo, e dopo poche richieste arriva OutOfMemoryError;
 *  2. con una cache limitata alle ultime 8 richieste: quando se ne aggiunge
 *     una nuova la piu' vecchia esce dalla coda, nessuno la raggiunge piu', e
 *     il GC la recupera. Diecimila richieste in 32 MB;
 *  3. senza cache: il risultato vive finche' serve a chi lo ha chiesto.
 *
 * Il punto: in Java un leak non e' memoria irraggiungibile (quella il GC la
 * recupera), e' memoria INUTILE che resta RAGGIUNGIBILE. La cura non e' una
 * free: e' togliere il riferimento (qui, removeFirst).
 *
 * Errore tipico evitato: nel catch di OutOfMemoryError, stampare prima di
 * liberare. Lo heap e' pieno: anche concatenare la stringa del messaggio
 * alloca, e puo' fallire di nuovo. Prima si svuota la cache (i megabyte
 * diventano irraggiungibili), poi si stampa. E non si chiama System.gc():
 * alla prossima allocazione il GC fara' da se'.
 */
class Servizio {
    static final int MEGA = 1024 * 1024;
    static final int RICHIESTE = 10_000;

    // --- 1. il leak ---
    static final List<byte[]> CACHE = new ArrayList<>();

    static byte[] conCacheInfinita(int richiesta) {
        byte[] risultato = new byte[MEGA];
        risultato[0] = (byte) richiesta;
        CACHE.add(risultato);                    // resta raggiungibile per sempre
        return risultato;
    }

    // --- 2. la cache con un limite ---
    static final int LIMITE = 8;
    static final Deque<byte[]> ULTIME = new ArrayDeque<>();

    static byte[] conCacheLimitata(int richiesta) {
        byte[] risultato = new byte[MEGA];
        risultato[0] = (byte) richiesta;
        if (ULTIME.size() == LIMITE) ULTIME.removeFirst();   // la piu' vecchia esce: e' la "free"
        ULTIME.addLast(risultato);
        return risultato;
    }

    // --- 3. nessuna cache ---
    static byte[] senzaCache(int richiesta) {
        byte[] risultato = new byte[MEGA];
        risultato[0] = (byte) richiesta;
        return risultato;                        // vive finche' serve al chiamante
    }

    public static void main(String[] args) {
        int servite = 0;
        try {
            for (int i = 0; i < RICHIESTE; i++) {
                conCacheInfinita(i);
                servite++;
            }
            System.out.println("cache infinita: " + servite + " richieste (heap molto grande?)");
        } catch (OutOfMemoryError e) {
            CACHE.clear();                       // PRIMA si libera, poi si stampa
            System.out.println("cache infinita: OutOfMemoryError dopo " + servite + " richieste");
        }

        long somma = 0;
        for (int i = 0; i < RICHIESTE; i++) somma += conCacheLimitata(i)[0];
        System.out.println("cache di " + LIMITE + ": " + RICHIESTE + " richieste servite, in cache "
                           + ULTIME.size() + " (controllo " + somma + ")");
        ULTIME.clear();

        somma = 0;
        for (int i = 0; i < RICHIESTE; i++) somma += senzaCache(i)[0];
        System.out.println("senza cache: " + RICHIESTE + " richieste servite (controllo " + somma + ")");
    }
}
