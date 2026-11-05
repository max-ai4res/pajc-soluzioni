package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import it.unibs.pajc.redolfi.e07.Protocollo.Azione;

/**
 * La sessione con la stanza: legge le righe del suo client, le interpreta con la tabella
 * del Protocollo, gli scrive le risposte. ENTRA, ESCE, /chi e i messaggi senza barra
 * restano nella stanza; i privati la attraversano. In produzione nasce da un Socket;
 * nei test da un Reader e un Writer (due stringhe), senza rete.
 */
public class Sessione implements Runnable {

    private static final int TIMEOUT_MS = 30 * 60 * 1000;   // mezz'ora di silenzio, poi lancia

    private final Socket socket;              // null senza rete
    private final Registro registro;          // lo stato condiviso, ricevuto nel costruttore
    private BufferedReader in;
    private PrintWriter out;
    private volatile String nome;             // null finche' non arriva /nome
    private volatile String stanza = Registro.GENERALE;   // la leggono i thread degli altri
    private boolean attiva = true;

    public Sessione(Socket socket, Registro registro) {
        this.socket = socket;
        this.registro = registro;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Registro registro) {
        this(null, registro);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    public String nome() {
        return nome;
    }

    public String stanza() {
        return stanza;
    }

    /** L'unico punto da cui si scrive al client: i thread degli altri non mescolano le righe. */
    public synchronized void invia(String riga) {
        out.println(riga);
    }

    @Override
    public void run() {
        log("collegato da " + socket.getRemoteSocketAddress());
        try {
            socket.setSoTimeout(TIMEOUT_MS);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (SocketTimeoutException e) {
            invia("ERR inattivo da troppo tempo");
        } catch (IOException e) {                                 // informazione, non un bug
            log(socket.isClosed() ? "chiusa dal server" : "connessione persa: " + e.getMessage());
        } finally {                                               // in TUTTI i modi di finire
            if (nome != null) {
                registro.rimuovi(nome);
                registro.aStanza(stanza, "ESCE " + nome);         // solo la stanza lo sa
            }
            chiudi();
            log("chiuso");
        }
    }

    /** Il dialogo: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) {      // null: il client ha chiuso
            elabora(riga);
        }
    }

    /** Da riga a comando: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) return;
        if (!riga.startsWith("/")) { tutti(riga.strip()); return; }       // senza barra: alla stanza
        String[] campi = riga.strip().split(" ", 2);                       // il comando e il resto
        Azione a = Protocollo.COMANDI.get(campi[0].toLowerCase());
        if (a == null) { invia("ERR comando sconosciuto: " + campi[0]); return; }
        String[] arg = Protocollo.argomenti(campi.length > 1 ? campi[1] : "", a.argomenti());
        if (arg == null) { invia("ERR uso: " + a.aiuto()); return; }
        a.esegui().accept(this, arg);
    }

    // le azioni della tabella

    void nome(String n) {
        if (nome != null) { invia("ERR hai gia' un nome: " + nome); return; }
        if (!Protocollo.nomeValido(n)) { invia("ERR nome non valido: " + n); return; }
        if (!registro.aggiungi(n, this)) { invia("ERR nome gia' in uso: " + n); return; }
        nome = n;
        invia("BENVENUTO " + nome);                                        // prima a chi entra
        registro.aStanzaAltri(stanza, "ENTRA " + nome, this);              // poi alla sua stanza
        log("si chiama " + nome);
    }

    void chi() {
        if (senzaNome()) return;
        invia("UTENTI " + String.join(",", registro.nomi(stanza)));
    }

    void tutti(String testo) {
        if (senzaNome()) return;
        registro.aStanza(stanza, "MSG " + nome + " " + testo);             // anche al mittente
    }

    void privato(String a, String testo) {
        if (senzaNome()) return;
        Sessione dest = registro.trova(a);                                 // in qualunque stanza
        if (dest == null) { invia("ERR utente sconosciuto: " + a); return; }
        String riga = "PRIV " + nome + " " + a + " " + testo;
        dest.invia(riga);                 // fra trova e invia il destinatario puo' andarsene
        if (dest != this) invia(riga);
    }

    /**
     * Cambiare stanza: ESCE alla vecchia, una sola scrittura del campo, STANZA a se',
     * ENTRA alla nuova. Un messaggio che passa proprio in quel momento puo' includere o
     * no chi si sta spostando: la finestra e' accettata.
     */
    void entra(String s) {
        if (senzaNome()) return;
        if (!Protocollo.stanzaValida(s)) { invia("ERR nome di stanza non valido: " + s); return; }
        if (s.equals(stanza)) { invia("ERR sei gia' in " + stanza); return; }
        registro.aStanzaAltri(stanza, "ESCE " + nome, this);
        stanza = s;
        invia("STANZA " + stanza);
        registro.aStanzaAltri(stanza, "ENTRA " + nome, this);
    }

    void stanze() {
        if (senzaNome()) return;
        invia("STANZE " + registro.stanze().entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",")));
    }

    void esci() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce, poi il finally
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
        invia("INFO una riga senza barra e' un messaggio per la tua stanza");
    }

    private boolean senzaNome() {
        if (nome != null) return false;
        invia("ERR prima scegli un nome con /nome");
        return true;
    }

    /** Chiude il socket: se la sessione e' ferma in readLine, il readLine lancia. */
    void chiudi() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // gia' chiuso
        }
    }

    private void log(String testo) {
        System.out.println("[" + (nome != null ? nome : "?") + "] " + testo);
    }
}
