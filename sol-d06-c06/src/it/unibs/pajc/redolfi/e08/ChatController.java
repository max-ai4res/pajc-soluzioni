package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

/**
 * Il Controller: manda al server le righe dell'utente e traduce le righe del server in
 * modifiche del Model con una tabella da parola ad azione, speculare a quella del
 * server. Tiene la Connessione, che e' l'unica cosa che sa di rete. Gira tutto
 * sull'EDT: la Connessione consegna le righe gia' li'.
 */
public class ChatController {
    private final ChatModel model;
    private final ChatView view;
    private Connessione connessione;

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public ChatController(ChatModel model, ChatView view) {
        this.model = model;
        this.view = view;
        view.onConnetti(this::connettiODisconnetti);
        view.onInvia(this::scritto);
        view.onUtenteScelto(view::preparaPrivato);
        tabellaRisposte();
    }

    private void tabellaRisposte() {
        risposte.put("INFO", model::sistema);
        risposte.put("BENVENUTO", nome -> {
            model.setNome(nome);
            connessione.invia("/chi");                          // e chiedo chi c'e'
        });
        risposte.put("UTENTI", elenco -> model.setUtenti(
                elenco.isEmpty() ? List.of() : List.of(elenco.split(","))));
        risposte.put("MSG", resto -> riga(ChatModel.Riga.Tipo.PUBBLICO, resto, 2));
        risposte.put("PRIV", resto -> riga(ChatModel.Riga.Tipo.PRIVATO, resto, 3));
        risposte.put("ENTRA", nome -> { model.entra(nome); model.sistema(nome + " e' entrato"); });
        risposte.put("ESCE", nome -> { model.esce(nome); model.sistema(nome + " e' uscito"); });
        risposte.put("ERR", this::errore);
        risposte.put("CIAO", resto -> model.sistema("il server ha chiuso la conversazione"));
    }

    // ------------------------------------------------------------ dall'utente

    private void connettiODisconnetti() {
        if (model.stato() == ChatModel.Stato.DISCONNESSO) {
            DialogConnessione.chiedi(view).ifPresent(this::connetti);
        } else {
            disconnetti();
        }
    }

    /** L'apertura puo' durare secondi (DNS, timeout): un SwingWorker, non l'EDT. */
    public void connetti(DialogConnessione.Parametri p) {
        model.svuota();                        // prima che arrivi la prima riga del server
        model.setStato(ChatModel.Stato.IN_CONNESSIONE);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(p.host(), p.porta(), ChatController.this::ricevuta,
                        ChatController.this::chiusa);
            }

            @Override
            protected void done() {
                try {
                    connessione = get();
                    model.setStato(ChatModel.Stato.CONNESSO);
                    model.sistema("Connesso a " + p.host() + ":" + p.porta());
                    connessione.invia("/nome " + p.nome());
                } catch (Exception e) {
                    model.setStato(ChatModel.Stato.DISCONNESSO);
                    model.errore("Impossibile connettersi a " + p.host() + ":" + p.porta()
                            + "\n" + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    public void disconnetti() {
        if (connessione != null) {
            connessione.invia("/esci");
            connessione.close();
            connessione = null;
        }
        model.setStato(ChatModel.Stato.DISCONNESSO);
    }

    /** Il testo scritto nel campo: parte com'e', una riga; il server ha la tabella. */
    public void scritto(String testo) {
        if (connessione == null) return;
        String riga = testo.replace('\n', ' ').strip();
        if (riga.isEmpty()) return;
        if (riga.equalsIgnoreCase("/esci")) { disconnetti(); return; }
        connessione.invia(riga);
    }

    // ------------------------------------------------------------ dal server (sull'EDT)

    void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> model.sistema("? " + riga)).accept(resto);
    }

    private void riga(ChatModel.Riga.Tipo tipo, String resto, int campi) {
        String[] c = Protocollo.argomenti(resto, campi);        // "da testo" o "da a testo"
        if (c == null) { model.sistema("? " + resto); return; }
        model.aggiungi(campi == 2 ? new ChatModel.Riga(tipo, c[0], null, c[1])
                                  : new ChatModel.Riga(tipo, c[0], c[1], c[2]));
    }

    private void errore(String motivo) {
        if (model.nome() == null) { model.errore(motivo); disconnetti(); }   // nome rifiutato
        else model.sistema("errore: " + motivo);
    }

    /** Il lettore e' finito: il server ha chiuso, o la connessione e' caduta. */
    void chiusa(String motivo) {
        if (connessione == null) return;               // disconnessi noi: gia' gestito
        connessione = null;
        model.setStato(ChatModel.Stato.DISCONNESSO);
        model.errore(motivo == null ? "Il server ha chiuso la connessione"
                                    : "Connessione persa: " + motivo);
    }

    /** Per le prove senza rete: riempie il Model come farebbe il server. */
    public static void simula(ChatModel model, String nome, List<String> utenti) {
        model.setStato(ChatModel.Stato.CONNESSO);
        model.setNome(nome);
        model.setUtenti(utenti);
    }
}
