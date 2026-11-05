package it.unibs.pajc.redolfi.e09;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * L'arbitro: accoppia i giocatori liberi due a due. Lo stato condiviso e' la coda di chi
 * aspetta; accoppia() e' synchronized perche' due sessioni possono entrare insieme e
 * devono vedere la stessa coda.
 */
public class Arbitro {
    private final Deque<Sessione> liberi = new ArrayDeque<>();

    /** Mette in attesa; se c'era gia' qualcuno, avvia la partita e la restituisce. */
    synchronized Partita accoppia(Sessione s) {
        Sessione altro = liberi.poll();
        if (altro == null || altro == s) {
            liberi.add(s);
            s.invia("INFO in attesa di un avversario...");
            return null;
        }
        Partita p = new Partita(altro, s);
        altro.partita(p);
        s.partita(p);
        p.inizia();
        return p;
    }

    synchronized void togli(Sessione s) { liberi.remove(s); }
}
