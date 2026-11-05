package it.unibs.pajc.redolfi.e08;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Il Model con le conversazioni separate: una per interlocutore ("Tutti" per i
 * messaggi pubblici, il nome dell'utente per i privati), con il contatore dei non letti
 * di ciascuna e la conversazione aperta. Avvisa con PropertyChangeSupport: "stato",
 * "nome", "utenti", "conversazione" (una nuova: il nome), "riga" (una riga aggiunta,
 * con la conversazione dentro), "nonLetti" (il nome della conversazione cambiata),
 * "aperta" (la conversazione ora aperta), "errore".
 */
public class ChatModel {

    public static final String TUTTI = "Tutti";

    public enum Stato { DISCONNESSO, IN_CONNESSIONE, CONNESSO }

    public record Riga(String conversazione, Tipo tipo, String da, String testo) {
        public enum Tipo { PUBBLICO, PRIVATO, SISTEMA }
    }

    private final PropertyChangeSupport ascoltatori = new PropertyChangeSupport(this);
    private Stato stato = Stato.DISCONNESSO;
    private String nome;
    private List<String> utenti = List.of();
    private final Map<String, List<Riga>> conversazioni = new LinkedHashMap<>();
    private final Map<String, Integer> nonLetti = new LinkedHashMap<>();
    private String aperta = TUTTI;
    private java.util.Map<String, Integer> stanze = java.util.Map.of();
    private String stanza = "generale";

    public ChatModel() { conversazioni.put(TUTTI, new ArrayList<>()); nonLetti.put(TUTTI, 0); }

    public void addPropertyChangeListener(PropertyChangeListener l) { ascoltatori.addPropertyChangeListener(l); }

    public Stato stato() { return stato; }
    public String nome() { return nome; }
    public List<String> utenti() { return utenti; }
    public List<String> conversazioni() { return List.copyOf(conversazioni.keySet()); }
    public List<Riga> righe(String conversazione) { return List.copyOf(conversazioni.getOrDefault(conversazione, List.of())); }
    public int nonLetti(String conversazione) { return nonLetti.getOrDefault(conversazione, 0); }
    public String aperta() { return aperta; }
    public java.util.Map<String, Integer> stanze() { return stanze; }
    public String stanza() { return stanza; }

    public void setStanze(java.util.Map<String, Integer> nuove) {
        var v = stanze; stanze = new java.util.TreeMap<>(nuove);
        ascoltatori.firePropertyChange("stanze", v, stanze);
    }

    public void setStanza(String nuova) {
        String v = stanza; stanza = nuova;
        ascoltatori.firePropertyChange("stanza", v, nuova);
    }

    public void setStato(Stato nuovo) { Stato v = stato; stato = nuovo; ascoltatori.firePropertyChange("stato", v, nuovo); }
    public void setNome(String nuovo) { String v = nome; nome = nuovo; ascoltatori.firePropertyChange("nome", v, nuovo); }
    public void setUtenti(List<String> nuovi) { List<String> v = utenti; utenti = List.copyOf(nuovi); ascoltatori.firePropertyChange("utenti", v, utenti); }

    public void entra(String u) {
        if (!utenti.contains(u)) { List<String> n = new ArrayList<>(utenti); n.add(u); n.sort(null); setUtenti(n); }
    }

    public void esce(String u) { List<String> n = new ArrayList<>(utenti); if (n.remove(u)) setUtenti(n); }

    /** Apre (se serve) la conversazione con quell'interlocutore e la restituisce. */
    public void apriConversazione(String con) {
        if (!conversazioni.containsKey(con)) {
            conversazioni.put(con, new ArrayList<>());
            nonLetti.put(con, 0);
            ascoltatori.firePropertyChange("conversazione", null, con);
        }
    }

    public void aggiungi(Riga riga) {
        apriConversazione(riga.conversazione());
        conversazioni.get(riga.conversazione()).add(riga);
        ascoltatori.firePropertyChange("riga", null, riga);
        if (!riga.conversazione().equals(aperta) && riga.tipo() != Riga.Tipo.SISTEMA) {
            nonLetti.merge(riga.conversazione(), 1, Integer::sum);
            ascoltatori.firePropertyChange("nonLetti", null, riga.conversazione());
        }
    }

    /** L'utente guarda questa conversazione: i suoi non letti tornano a zero. */
    public void apri(String con) {
        apriConversazione(con);
        aperta = con;
        if (nonLetti.get(con) != 0) {
            nonLetti.put(con, 0);
            ascoltatori.firePropertyChange("nonLetti", null, con);
        }
        ascoltatori.firePropertyChange("aperta", null, con);
    }

    public void sistema(String testo) { aggiungi(new Riga(TUTTI, Riga.Tipo.SISTEMA, null, testo)); }
    public void errore(String testo) { ascoltatori.firePropertyChange("errore", null, testo); }

    public void svuota() {
        conversazioni.keySet().removeIf(k -> !k.equals(TUTTI));
        conversazioni.get(TUTTI).clear();
        nonLetti.clear();
        nonLetti.put(TUTTI, 0);
        aperta = TUTTI;
        ascoltatori.firePropertyChange("svuota", null, TUTTI);
        setUtenti(List.of());
        setNome(null);
    }
}
