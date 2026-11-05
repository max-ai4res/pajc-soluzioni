package it.unibs.pajc.redolfi.e06;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * L'arbitro: accoppia i giocatori liberi due a due e tiene il registro delle partite in
 * corso, per gli spettatori. Lo stato condiviso e' la coda di chi aspetta; accoppia() e'
 * synchronized perche' due sessioni possono entrare insieme e devono vedere la stessa
 * coda.
 */
public class Arbitro {
    private final Deque<Sessione> liberi = new ArrayDeque<>();
    private final Map<String, Partita> inCorso = new ConcurrentHashMap<>();

    public List<String> partite() { return inCorso.keySet().stream().sorted().toList(); }
    public Partita partita(String nome) { return inCorso.get(nome); }
    void finita(Partita p) { inCorso.remove(p.nome()); }

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
        inCorso.put(p.nome(), p);
        p.inizia();
        return p;
    }

    synchronized void togli(Sessione s) { liberi.remove(s); }
}
