package it.unibs.pajc.redolfi.e04;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Lo stato globale del quiz, condiviso fra le sessioni, il thread della console e il
 * timer: i giocatori, la domanda corrente, se e' aperta, i punti. Ogni metodo che lo
 * legge e lo cambia e' synchronized: "il primo che risponde giusto" e' un check-then-act
 * (la domanda e' ancora aperta? allora il punto e' tuo e la chiudo) e va fatto in una
 * sola sezione critica. Le righe a tutti partono da qui, con il lock preso: sono
 * println in un buffer, durano microsecondi.
 */
public class Quiz {
    public record Quesito(String domanda, String risposta) {}

    private final List<Quesito> quesiti;
    private final ConcurrentHashMap<String, Sessione> giocatori = new ConcurrentHashMap<>();
    private final Map<String, Integer> punti = new TreeMap<>();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "timer-quiz"); t.setDaemon(true); return t;
    });
    private int corrente = -1;                // indice della domanda; -1 non iniziato
    private boolean aperta;
    private boolean finito;
    private ScheduledFuture<?> scadenza;

    public Quiz(List<Quesito> quesiti) { this.quesiti = quesiti; }

    /** true se il nome era libero; chi entra a quiz iniziato riceve subito la domanda. */
    public synchronized boolean aggiungi(String nome, Sessione s) {
        if (giocatori.putIfAbsent(nome, s) != null) return false;
        punti.put(nome, 0);
        if (corrente >= 0 && !finito) s.invia(domandaCorrente());
        return true;
    }

    public synchronized void rimuovi(String nome) { giocatori.remove(nome); }

    public synchronized Map<String, Integer> classifica() { return new TreeMap<>(punti); }

    /** Dalla console: "via" fa partire il quiz. */
    public synchronized void via() {
        if (corrente >= 0) return;
        aTutti("INFO Si comincia!");
        prossima();
    }

    private synchronized void prossima() {
        corrente++;
        if (corrente >= quesiti.size()) {
            finito = true;
            aperta = false;
            aTutti("FINE " + Protocollo.classifica(classifica()));
            return;
        }
        aperta = true;
        aTutti(domandaCorrente());
        if (scadenza != null) scadenza.cancel(false);
        int questa = corrente;
        scadenza = timer.schedule(() -> scaduta(questa), 20, TimeUnit.SECONDS);
    }

    private synchronized void scaduta(int questa) {
        if (questa != corrente || !aperta) return;      // qualcuno ha gia' risposto
        aperta = false;
        aTutti("TEMPO " + quesiti.get(corrente).risposta());
        timer.schedule(this::prossima, 2, TimeUnit.SECONDS);
    }

    /** La risposta di un giocatore: il primo giusto chiude la domanda. */
    public synchronized void risposta(String nome, Sessione s, String testo) {
        if (!aperta) { s.invia("ERR nessuna domanda aperta"); return; }
        Quesito q = quesiti.get(corrente);
        if (!testo.strip().equalsIgnoreCase(q.risposta())) { s.invia("NO"); return; }
        aperta = false;                                  // check-then-act sotto il lock
        punti.merge(nome, 1, Integer::sum);
        aTutti("PUNTO " + nome + " " + q.risposta());
        timer.schedule(this::prossima, 2, TimeUnit.SECONDS);
    }

    private String domandaCorrente() {
        return "DOMANDA " + (corrente + 1) + " " + quesiti.get(corrente).domanda();
    }

    private void aTutti(String riga) { for (Sessione s : giocatori.values()) s.invia(riga); }
}
