package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

/**
 * Esercizio 5.8 - il client di WhatsPAJC con le conversazioni separate.
 *
 * Il Model tiene una conversazione per interlocutore (ChatModel) e la View una scheda
 * per conversazione. Il Controller traduce le righe del server con una tabella da
 * parola ad azione, speculare a quella dei comandi del server, e decide in quale
 * conversazione va ogni riga: un MSG in "Tutti", un PRIV nella conversazione con l'ALTRO
 * (se "da" sono io, e' il destinatario; altrimenti il mittente). Scrivere in una scheda
 * privata manda un /a a quell'utente senza doverlo scrivere (a meno che il testo non sia
 * gia' un comando); il doppio click su un utente apre e seleziona la sua scheda. Le righe
 * dell'utente partono com'e': la tabella che le controlla e' nel server, e una riga
 * sbagliata torna come ERR. I non letti li conta il Model (riga in una conversazione non
 * aperta) e li mostra la View nel titolo della scheda; cambiare scheda avvisa il Model,
 * che li azzera.
 *
 * Errore tipico: contare i non letti nella View guardando la scheda selezionata: il
 * conteggio e' stato, e lo stato sta nel Model, altrimenti una seconda vista (o un test)
 * non lo vede.
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
        view.onScheda(model::apri);
        view.onUtenteScelto(u -> { if (!u.equals(model.nome())) model.apri(u); });
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
        risposte.put("MSG", this::pubblico);
        risposte.put("PRIV", this::privato);
        risposte.put("ENTRA", nome -> { model.entra(nome); model.sistema(nome + " e' entrato"); });
        risposte.put("ESCE", nome -> { model.esce(nome); model.sistema(nome + " e' uscito"); });
        risposte.put("ERR", this::errore);
        risposte.put("CIAO", resto -> model.sistema("il server ha chiuso la conversazione"));
    }

    // ------------------------------------------------------------ dall'utente

    private void connettiODisconnetti() {
        if (model.stato() == ChatModel.Stato.DISCONNESSO) DialogConnessione.chiedi(view).ifPresent(this::connetti);
        else disconnetti();
    }

    /** L'apertura puo' durare secondi (DNS, timeout): un SwingWorker, non l'EDT. */
    public void connetti(DialogConnessione.Parametri p) {
        model.svuota();                        // prima che arrivi la prima riga del server
        model.setStato(ChatModel.Stato.IN_CONNESSIONE);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(p.host(), p.porta(), ChatController.this::ricevuta, ChatController.this::chiusa);
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
                    model.errore("Impossibile connettersi: " + e.getCause().getMessage());
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

    /** Il testo scritto nella scheda aperta: in una scheda privata diventa un /a; poi parte com'e'. */
    public void scritto(String testo) {
        if (connessione == null) return;
        String riga = testo.replace('\n', ' ').strip();
        if (riga.isEmpty()) return;
        String aperta = model.aperta();
        if (!aperta.equals(ChatModel.TUTTI) && !riga.startsWith("/")) riga = "/a " + aperta + " " + riga;
        if (riga.equalsIgnoreCase("/esci")) { disconnetti(); return; }
        connessione.invia(riga);
    }

    // ------------------------------------------------------------ dal server (sull'EDT)

    void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> model.sistema("? " + riga)).accept(resto);
    }

    /** MSG da testo: nella conversazione Tutti. */
    private void pubblico(String resto) {
        String[] c = Protocollo.argomenti(resto, 2);
        if (c == null) { model.sistema("? " + resto); return; }
        model.aggiungi(new ChatModel.Riga(ChatModel.TUTTI, ChatModel.Riga.Tipo.PUBBLICO, c[0], c[1]));
    }

    /** PRIV da a testo: nella conversazione con l'altro, chiunque dei due sia io. */
    private void privato(String resto) {
        String[] c = Protocollo.argomenti(resto, 3);
        if (c == null) { model.sistema("? " + resto); return; }
        String altro = c[0].equals(model.nome()) ? c[1] : c[0];
        model.aggiungi(new ChatModel.Riga(altro, ChatModel.Riga.Tipo.PRIVATO, c[0], c[2]));
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
        model.errore(motivo == null ? "Il server ha chiuso la connessione" : "Connessione persa: " + motivo);
    }
}
