package it.unibs.pajc.redolfi.e07;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lo stato del sondaggio sul server: la domanda aperta, le risposte, chi ha votato cosa.
 * Il voto doppio e' rifiutato in una mossa con putIfAbsent sulla mappa nome -> indice; i
 * conteggi si ricavano dalla mappa (nessun contatore da tenere allineato). Chi ascolta
 * (le sessioni e la finestra del docente) riceve le righe del protocollo.
 */
public class Sondaggio {
    private volatile String testo;
    private volatile List<String> risposte = List.of();
    private final ConcurrentHashMap<String, Integer> voti = new ConcurrentHashMap<>();
    private final Set<Consumer<String>> ascoltatori = ConcurrentHashMap.newKeySet();

    public void ascolta(Consumer<String> a) { ascoltatori.add(a); }
    public void dimentica(Consumer<String> a) { ascoltatori.remove(a); }

    public synchronized void apri(String testo, List<String> risposte) {
        this.testo = testo;
        this.risposte = List.copyOf(risposte);
        voti.clear();
        aTutti(Protocollo.domanda(testo, risposte));
        aTutti(Protocollo.voti(conteggi()));
    }

    public synchronized void chiudi() {
        testo = null;
        aTutti("CHIUSA");
    }

    /** true se il voto e' stato contato (domanda aperta, indice valido, primo voto del nome). */
    public String vota(String nome, int k) {
        List<String> r = risposte;
        if (testo == null) return "ERR nessuna domanda aperta";
        if (k < 0 || k >= r.size()) return "ERR risposta inesistente";
        if (voti.putIfAbsent(nome, k) != null) return "ERR hai gia' votato";
        aTutti(Protocollo.voti(conteggi()));
        return "OK";
    }

    public int[] conteggi() {
        int[] c = new int[risposte.size()];
        for (int k : voti.values()) if (k < c.length) c[k]++;
        return c;
    }

    /** La domanda corrente come righe, per chi entra adesso (null se chiusa). */
    public List<String> corrente() {
        String t = testo;
        return t == null ? List.of() : List.of(Protocollo.domanda(t, risposte), Protocollo.voti(conteggi()));
    }

    private void aTutti(String riga) { for (Consumer<String> a : ascoltatori) a.accept(riga); }
}
