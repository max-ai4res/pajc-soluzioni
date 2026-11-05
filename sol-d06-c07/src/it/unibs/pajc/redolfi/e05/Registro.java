package it.unibs.pajc.redolfi.e05;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lo stato condiviso fra le sessioni: chi e' collegato, con quale nome. ConcurrentHashMap
 * e putIfAbsent (un'operazione sola). Il server lo crea e lo passa a ogni Sessione.
 */
public class Registro {
    private final ConcurrentHashMap<String, Sessione> sessioni = new ConcurrentHashMap<>();

    /** true se il nome era libero ed e' stato preso. */
    public boolean aggiungi(String nome, Sessione s) {
        return sessioni.putIfAbsent(nome, s) == null;
    }

    public void rimuovi(String nome) { sessioni.remove(nome); }

    /** La sessione con quel nome, o null se non c'e' (piu'). */
    public Sessione trova(String nome) { return sessioni.get(nome); }

    public List<String> nomi() { return sessioni.keySet().stream().sorted().toList(); }

    public int quanti() { return sessioni.size(); }

    /** Una riga a tutti i collegati, mittente compreso. Gira sul thread di chi la manda. */
    public void aTutti(String riga) {
        for (Sessione s : sessioni.values()) s.invia(riga);
    }

    public void aAltri(String riga, Sessione tranne) {
        for (Sessione s : sessioni.values())
            if (s != tranne) s.invia(riga);
    }
}
