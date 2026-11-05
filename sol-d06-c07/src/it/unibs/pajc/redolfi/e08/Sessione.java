package it.unibs.pajc.redolfi.e08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e08.Protocollo.Azione;

/**
 * La sessione di WhatsPAJC completa: legge le righe del client, le interpreta con la
 * tabella del Protocollo (un metodo per azione), gli scrive le risposte; in piu' la
 * stanza in cui sta, i privati fra stanze, /ping e il timeout a scatti (un minuto di
 * silenzio: niente; due: "sei ancora li'?"; tre: CIAO e chiusura). In produzione nasce
 * da un Socket; nei test da un Reader e un Writer (due stringhe).
 */
public class Sessione implements Runnable {
    public static final int TIMEOUT_MS = 60_000;

    private final Socket socket;               // null senza rete
    private final Registro registro;
    private BufferedReader in;
    private PrintWriter out;
    private volatile String nome;              // null finche' non arriva /nome
    private volatile String stanza = Registro.GENERALE;
    private int scaduti;                       // timeout consecutivi senza una riga
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

    public String nome() { return nome; }
    public String stanza() { return stanza; }

    /** L'unico punto da cui si scrive al client: due thread insieme non mescolano le righe. */
    public synchronized void invia(String riga) {
        out.println(riga);
    }

    public void chiudi() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // gia' chiuso
        }
    }

    @Override
    public void run() {
        log("collegato da " + socket.getRemoteSocketAddress());
        try {
            socket.setSoTimeout(TIMEOUT_MS);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (IOException e) {
            log(socket.isClosed() ? "chiusa dal server" : "connessione persa: " + e.getMessage());
        } finally {                                               // in TUTTI i modi di finire
            if (nome != null) {
                registro.rimuovi(nome);
                registro.aStanza(stanza, "ESCE " + nome);
            }
            chiudi();
            log("chiuso");
        }
    }

    /** Il dialogo, con il timeout a scatti: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome");
        while (attiva) {
            String riga;
            try {
                riga = in.readLine();
            } catch (SocketTimeoutException e) {        // un minuto senza righe
                scaduti++;
                if (scaduti == 2) invia("INFO sei ancora li'?");
                if (scaduti >= 3) { invia("CIAO"); return; }
                continue;
            }
            if (riga == null) return;                   // il client ha chiuso
            scaduti = 0;
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
        registro.aStanzaAltri(stanza, "ENTRA " + nome, this);              // poi alla stanza
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
        Sessione dest = registro.trova(a);
        if (dest == null) { invia("ERR utente sconosciuto: " + a); return; }
        String riga = "PRIV " + nome + " " + a + " " + testo;
        dest.invia(riga);                 // fra trova e invia il destinatario puo' andarsene
        if (dest != this) invia(riga);
    }

    /** /entra S: ESCE alla vecchia stanza, STANZA a me, ENTRA alla nuova. */
    void entra(String nuova) {
        if (senzaNome()) return;
        if (!Protocollo.stanzaValida(nuova)) { invia("ERR nome di stanza non valido: " + nuova); return; }
        if (nuova.equals(stanza)) { invia("ERR sei gia' in " + stanza); return; }
        registro.aStanzaAltri(stanza, "ESCE " + nome, this);
        stanza = nuova;
        invia("STANZA " + stanza);
        registro.aStanzaAltri(stanza, "ENTRA " + nome, this);
    }

    void stanze() {
        if (senzaNome()) return;
        invia("STANZE " + Protocollo.stanze(registro.stanze()));
    }

    /** /ping: vale anche prima del nome; azzera il conteggio dei timeout (lo fa ogni riga). */
    void ping() {
        invia("PONG");
    }

    void esci() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce, poi il finally
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
        invia("INFO una riga senza barra e' un messaggio per la tua stanza");
        invia("INFO dalla finestra: Foto... manda una foto, /vedi nome la apre");
    }

    private boolean senzaNome() {
        if (nome != null) return false;
        invia("ERR prima scegli un nome con /nome");
        return true;
    }

    private void log(String testo) {
        System.out.printf("[%s] %s%n", nome != null ? nome : socket.getRemoteSocketAddress(), testo);
    }
}
