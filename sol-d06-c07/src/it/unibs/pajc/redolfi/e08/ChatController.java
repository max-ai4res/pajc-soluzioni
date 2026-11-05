package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Palestra 7.8 - WhatsPAJC completa, il client: conversazioni separate con i non letti,
 * stanze in una tendina, Foto... (METTI agli allegati in un worker) e /vedi nome (PRENDI
 * in un worker, dialogo con l'immagine), dialogo di connessione con Cerca (UDP),
 * riconnessione automatica dopo una caduta del server (ultimi parametri, un Timer).
 *
 * Il Controller manda al server le righe dell'utente e traduce le righe del server in
 * modifiche del Model con una tabella da parola ad azione, speculare a quella dei
 * comandi sul server: una parola sconosciuta finisce nella conversazione come riga di
 * sistema. Tiene la Connessione, che e' l'unica cosa che sa di rete. Gira tutto
 * sull'EDT: la Connessione consegna le righe gia' li'.
 *
 * Dalla versione con le schede:
 * Il Model tiene una conversazione per interlocutore (ChatModel) e la View una scheda
 * per conversazione. Il Controller decide in quale conversazione va ogni riga: un MSG
 * in "Tutti", un PRIV nella conversazione con l'ALTRO (se "da" sono io, e' il
 * destinatario; altrimenti il mittente). Scrivere in una scheda privata manda un /a a
 * quell'utente senza doverlo scrivere (a meno che il testo non sia gia' un comando);
 * il doppio click su un utente apre e seleziona la sua scheda. I non letti li conta il
 * Model (riga in una conversazione non aperta) e li mostra la View nel titolo della
 * scheda; cambiare scheda avvisa il Model, che li azzera.
 *
 * Errore tipico: contare i non letti nella View guardando la scheda selezionata: il
 * conteggio e' stato, e lo stato sta nel Model, altrimenti una seconda vista (o un test)
 * non lo vede.
 */
public class ChatController {
    private final ChatModel model;
    private final ChatView view;
    private Connessione connessione;
    private DialogConnessione.Parametri ultimi;      // per riconnettersi

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public ChatController(ChatModel model, ChatView view) {
        this.model = model;
        this.view = view;
        view.onConnetti(this::connettiODisconnetti);
        view.onInvia(this::scritto);
        view.onScheda(model::apri);
        view.onUtenteScelto(u -> { if (!u.equals(model.nome())) model.apri(u); });
        view.onStanza(st -> { if (connessione != null && !st.equals(model.stanza())) connessione.invia("/entra " + st); });
        view.onFoto(this::scegliFoto);
        tabellaRisposte();
    }

    private void tabellaRisposte() {
        risposte.put("INFO", model::sistema);
        risposte.put("BENVENUTO", nome -> {
            model.setNome(nome);
            connessione.invia("/chi");                          // e chiedo chi c'e'
            connessione.invia("/stanze");
        });
        risposte.put("UTENTI", elenco -> model.setUtenti(
                elenco.isEmpty() ? List.of() : List.of(elenco.split(","))));
        risposte.put("MSG", resto -> {
            String[] c = Protocollo.argomenti(resto, 2);                             // da testo
            if (c == null) { model.sistema("? " + resto); return; }
            model.aggiungi(new ChatModel.Riga(ChatModel.TUTTI, ChatModel.Riga.Tipo.PUBBLICO, c[0], c[1]));
        });
        risposte.put("PRIV", resto -> {
            String[] c = Protocollo.argomenti(resto, 3);                             // da a testo
            if (c == null) { model.sistema("? " + resto); return; }
            String altro = c[0].equals(model.nome()) ? c[1] : c[0];
            model.aggiungi(new ChatModel.Riga(altro, ChatModel.Riga.Tipo.PRIVATO, c[0], c[2]));
        });
        risposte.put("ENTRA", nome -> { model.entra(nome); model.sistema(nome + " e' entrato"); });
        risposte.put("ESCE", nome -> { model.esce(nome); model.sistema(nome + " e' uscito"); });
        risposte.put("ERR", this::errore);
        risposte.put("CIAO", resto -> model.sistema("il server ha chiuso la conversazione"));
        risposte.put("STANZE", elenco -> model.setStanze(Protocollo.stanze(elenco)));
        risposte.put("STANZA", nome -> {
            model.setStanza(nome);
            model.sistema("sei nella stanza " + nome);
            connessione.invia("/chi");
            connessione.invia("/stanze");
        });
        risposte.put("PONG", resto -> model.sistema("pong"));
        risposte.put("FOTO", resto -> {
            String[] c = Protocollo.argomenti(resto, 2);                             // da nome
            if (c == null) { model.sistema("? " + resto); return; }
            model.aggiungi(new ChatModel.Riga(ChatModel.TUTTI, ChatModel.Riga.Tipo.SISTEMA, null,
                    c[0] + " ha mandato la foto " + c[1] + " (scrivi /vedi " + c[1] + " per aprirla)"));
        });
    }

    // ------------------------------------------------------------ dall'utente

    private void connettiODisconnetti() {
        if (model.stato() == ChatModel.Stato.DISCONNESSO) DialogConnessione.chiedi(view).ifPresent(this::connetti);
        else disconnetti();
    }

    /** L'apertura puo' durare secondi (DNS, timeout): un SwingWorker, non l'EDT. */
    public void connetti(DialogConnessione.Parametri p) {
        ultimi = p;
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

    /**
     * Il testo scritto nella scheda aperta: parte com'e', una riga; il server ha la
     * tabella. In una scheda privata diventa un /a; /vedi e' un comando del client, /esci
     * chiude anche da qui.
     */
    public void scritto(String testo) {
        if (connessione == null) return;
        String riga = testo.replace('\n', ' ').strip();
        if (riga.isEmpty()) return;
        if (riga.startsWith("/vedi ")) { vediFoto(riga.substring(6).strip()); return; }     // comando del client
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

    private void errore(String motivo) {
        if (model.nome() == null) { model.errore(motivo); disconnetti(); }   // nome rifiutato
        else model.sistema("errore: " + motivo);
    }

    /** Il lettore e' finito: il server ha chiuso, o la connessione e' caduta (si riprova). */
    void chiusa(String motivo) {
        if (connessione == null) return;               // disconnessi noi: gia' gestito
        connessione = null;
        model.setStato(ChatModel.Stato.DISCONNESSO);
        if (motivo == null) { model.errore("Il server ha chiuso la connessione"); return; }
        model.sistema("connessione persa (" + motivo + "): riprovo fra 5 secondi...");
        Timer t = new Timer(5000, e -> {              // riconnessione
            if (model.stato() == ChatModel.Stato.DISCONNESSO && ultimi != null) connetti(ultimi);
        });
        t.setRepeats(false);
        t.start();
    }

    /** "Foto...": un file scelto e mandato agli allegati in un SwingWorker. */
    private void scegliFoto() {
        if (connessione == null || ultimi == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Immagini", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) return;
        Path file = fc.getSelectedFile().toPath();
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws IOException {
                AllegatiClient.metti(ultimi.host(), ultimi.porta() + 1, model.nome(), file);
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception e) { model.errore("Foto non inviata: " + e.getCause().getMessage()); }
            }
        }.execute();
    }

    /** "/vedi nome": la foto presa dagli allegati in un SwingWorker e mostrata dalla View. */
    private void vediFoto(String nome) {
        if (ultimi == null) return;
        new SwingWorker<byte[], Void>() {
            @Override protected byte[] doInBackground() throws IOException {
                return AllegatiClient.prendi(ultimi.host(), ultimi.porta() + 1, nome);
            }
            @Override protected void done() {
                try {
                    byte[] dati = get();
                    if (dati.length == 0) model.errore("Il server non ha la foto " + nome);
                    else view.mostraFoto(nome, dati);
                } catch (Exception e) { model.errore("Foto non scaricata: " + e.getCause().getMessage()); }
            }
        }.execute();
    }
}
