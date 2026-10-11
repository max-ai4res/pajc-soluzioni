package it.unibs.pajc.redolfi.e10;

import java.util.ArrayList;
import java.util.List;

/**
 * Esercizio 5.10: un registro a piu' scrittori con istantanee coerenti.
 *
 * Lo stato (la lista) e' protetto da un lock privato. Dentro la sezione critica ci sta
 * solo l'add e la copia; la costruzione dell'Evento (System.nanoTime, il nome del thread)
 * avviene fuori. L'istantanea e' una copia immutabile (List.copyOf) fatta sotto lock:
 * il lettore riceve una lista che nessuno puo' piu' cambiare, ne' lui ne' gli scrittori,
 * e gli scrittori ripartono subito. La copia costa O(n) ma si paga solo quando il
 * lettore la chiede.
 */
public class Registro {
    public record Evento(long istante, String thread, String testo) { }

    private final Object lock = new Object();
    private final List<Evento> eventi = new ArrayList<>();

    public void aggiungi(String testo) {
        Evento e = new Evento(System.nanoTime(), Thread.currentThread().getName(), testo); // fuori
        synchronized (lock) {
            eventi.add(e);                                                              // dentro
        }
    }

    public List<Evento> istantanea() {
        synchronized (lock) {
            return List.copyOf(eventi);         // copia immutabile, fatta sotto lock
        }
    }
}
