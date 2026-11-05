package it.unibs.pajc.redolfi.e08;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Lo stato del client: non sa nulla di socket e di Swing. Avvisa chi ascolta con
 * PropertyChangeSupport: "stato", "nome", "utenti" (la lista nuova), "riga" (la riga
 * aggiunta alla conversazione), "errore" (un testo da mostrare). Vive sull'EDT.
 */
public class ChatModel {

    public enum Stato { DISCONNESSO, IN_CONNESSIONE, CONNESSO }

    /** Una riga della conversazione. */
    public record Riga(Tipo tipo, String da, String a, String testo) {
        public enum Tipo { PUBBLICO, PRIVATO, SISTEMA }
    }

    private final PropertyChangeSupport ascoltatori = new PropertyChangeSupport(this);
    private Stato stato = Stato.DISCONNESSO;
    private String nome;
    private List<String> utenti = List.of();
    private final List<Riga> conversazione = new ArrayList<>();

    public void addPropertyChangeListener(PropertyChangeListener l) {
        ascoltatori.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        ascoltatori.removePropertyChangeListener(l);
    }

    public Stato stato() { return stato; }
    public String nome() { return nome; }
    public List<String> utenti() { return utenti; }
    public List<Riga> conversazione() { return List.copyOf(conversazione); }

    public void setStato(Stato nuovo) {
        Stato vecchio = stato;
        stato = nuovo;
        ascoltatori.firePropertyChange("stato", vecchio, nuovo);
    }

    public void setNome(String nuovo) {
        String vecchio = nome;
        nome = nuovo;
        ascoltatori.firePropertyChange("nome", vecchio, nuovo);
    }

    public void setUtenti(List<String> nuovi) {
        List<String> vecchi = utenti;
        utenti = List.copyOf(nuovi);
        ascoltatori.firePropertyChange("utenti", vecchi, utenti);
    }

    public void entra(String utente) {
        if (!utenti.contains(utente)) {
            List<String> nuovi = new ArrayList<>(utenti);
            nuovi.add(utente);
            nuovi.sort(null);
            setUtenti(nuovi);
        }
    }

    public void esce(String utente) {
        List<String> nuovi = new ArrayList<>(utenti);
        if (nuovi.remove(utente)) setUtenti(nuovi);
    }

    public void aggiungi(Riga riga) {
        conversazione.add(riga);
        ascoltatori.firePropertyChange("riga", null, riga);
    }

    public void sistema(String testo) {
        aggiungi(new Riga(Riga.Tipo.SISTEMA, null, null, testo));
    }

    /** Un errore da mostrare all'utente: non e' stato, e' un evento. */
    public void errore(String testo) {
        ascoltatori.firePropertyChange("errore", null, testo);
    }

    public void svuota() {
        conversazione.clear();
        setUtenti(List.of());
        setNome(null);
    }
}
