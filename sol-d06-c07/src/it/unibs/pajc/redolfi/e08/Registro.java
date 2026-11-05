package it.unibs.pajc.redolfi.e08;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Il registro con le stanze: oltre alla mappa nome -> sessione, ogni sessione sa in quale
 * stanza e' (campo in Sessione) e il registro risponde alle domande "chi c'e' nella
 * stanza X" scorrendo le sessioni. Non c'e' una seconda mappa stanza -> insieme da tenere
 * allineata: una stanza esiste finche' qualcuno ci sta, e "scompare" da sola. Lo
 * spostamento e' una sola scrittura (il campo stanza della sessione, volatile), e un
 * aStanza che scorre nel frattempo vede l'utente nella vecchia o nella nuova stanza: la
 * finestra e' accettata e documentata.
 */
public class Registro {
    public static final String GENERALE = "generale";
    private final ConcurrentHashMap<String, Sessione> sessioni = new ConcurrentHashMap<>();

    /** true se il nome era libero ed e' stato preso. */
    public boolean aggiungi(String nome, Sessione s) { return sessioni.putIfAbsent(nome, s) == null; }
    public void rimuovi(String nome) { sessioni.remove(nome); }
    public Sessione trova(String nome) { return sessioni.get(nome); }
    public int quanti() { return sessioni.size(); }
    public List<String> nomi() { return sessioni.keySet().stream().sorted().toList(); }

    /** I nomi di chi sta nella stanza. */
    public List<String> nomi(String stanza) {
        return sessioni.values().stream().filter(s -> stanza.equals(s.stanza()))
                .map(Sessione::nome).sorted().toList();
    }

    /** Le stanze con il numero di utenti, in ordine alfabetico. */
    public Map<String, Integer> stanze() {
        Map<String, Integer> m = new TreeMap<>();
        for (Sessione s : sessioni.values()) m.merge(s.stanza(), 1, Integer::sum);
        return m;
    }

    /** Una riga a tutti i collegati, in ogni stanza. Gira sul thread di chi la manda. */
    public void aTutti(String riga) { for (Sessione s : sessioni.values()) s.invia(riga); }

    public void aStanza(String stanza, String riga) {
        for (Sessione s : sessioni.values()) if (stanza.equals(s.stanza())) s.invia(riga);
    }

    public void aStanzaAltri(String stanza, String riga, Sessione tranne) {
        for (Sessione s : sessioni.values()) if (s != tranne && stanza.equals(s.stanza())) s.invia(riga);
    }
}
