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
 * La sessione che misura il silenzio: readLine scade ogni timeoutMs con una
 * SocketTimeoutException, catturata DENTRO il ciclo di dialoga, e la sessione conta gli
 * "scaduti" consecutivi: al secondo avvisa, al terzo saluta e chiude. Qualunque riga
 * azzera il conteggio, /ping compreso, che risponde PONG e non tocca gli altri. Il resto
 * (tabella, azioni, invia sincronizzato, finally) e' la Sessione della dispensa. In
 * produzione nasce da un Socket; nei test da un Reader e un Writer, senza rete.
 */
public class Sessione implements Runnable {

    /** Un minuto: l'unita' di misura del silenzio (avviso dopo due, uscita dopo tre). */
    public static final int TIMEOUT_MS = 60_000;

    private final Socket socket;              // null senza rete
    private final Registro registro;          // lo stato condiviso, ricevuto nel costruttore
    private final int timeoutMs;
    private BufferedReader in;
    private PrintWriter out;
    private String nome;                      // null finche' non arriva /nome
    private boolean attiva = true;
    private int scaduti;                      // timeout consecutivi senza una riga

    public Sessione(Socket socket, Registro registro, int timeoutMs) {
        this.socket = socket;
        this.registro = registro;
        this.timeoutMs = timeoutMs;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Registro registro) {
        this(null, registro, TIMEOUT_MS);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    public String nome() {
        return nome;
    }

    /** L'unico punto da cui si scrive al client: i thread degli altri non mescolano le righe. */
    public synchronized void invia(String riga) {
        out.println(riga);
    }

    @Override
    public void run() {
        log("collegato da " + socket.getRemoteSocketAddress());
        try {
            socket.setSoTimeout(timeoutMs);                   // readLine scade: e' l'orologio
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (IOException e) {                                 // informazione, non un bug
            log(socket.isClosed() ? "chiusa dal server" : "connessione persa: " + e.getMessage());
        } finally {                                               // in TUTTI i modi di finire
            if (nome != null) {
                registro.rimuovi(nome);
                registro.aTutti("ESCE " + nome);
            }
            chiudi();
            log("chiuso");
        }
    }

    /**
     * Il dialogo, con il catch del timeout dentro il while: ogni scadenza e' un tic
     * dell'orologio, non la fine. Nei test si chiama direttamente, su due stringhe.
     */
    void dialoga() throws IOException {
        invia("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome");
        while (attiva) {
            String riga;
            try {
                riga = in.readLine();
            } catch (SocketTimeoutException e) {                  // timeoutMs senza una riga
                scaduti++;
                if (scaduti == 2) invia("INFO sei ancora li'?");
                if (scaduti >= 3) { invia("CIAO"); return; }
                continue;
            }
            if (riga == null) return;                             // il client ha chiuso
            scaduti = 0;                                          // qualunque riga azzera
            elabora(riga);
        }
    }

    /** Da riga a comando: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) return;
        if (!riga.startsWith("/")) { tutti(riga.strip()); return; }       // senza barra: a tutti
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
        registro.aAltri("ENTRA " + nome, this);                            // poi agli altri
        log("si chiama " + nome);
    }

    void chi() {
        if (senzaNome()) return;
        invia("UTENTI " + String.join(",", registro.nomi()));
    }

    void tutti(String testo) {
        if (senzaNome()) return;
        registro.aTutti("MSG " + nome + " " + testo);                      // anche al mittente
    }

    void privato(String a, String testo) {
        if (senzaNome()) return;
        Sessione dest = registro.trova(a);
        if (dest == null) { invia("ERR utente sconosciuto: " + a); return; }
        String riga = "PRIV " + nome + " " + a + " " + testo;
        dest.invia(riga);                 // fra trova e invia il destinatario puo' andarsene
        if (dest != this) invia(riga);
    }

    /** Anche senza nome: il conteggio lo ha gia' azzerato dialoga, qui resta solo la risposta. */
    void ping() {
        invia("PONG");
    }

    void esci() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce, poi il finally
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
        invia("INFO una riga senza barra e' un messaggio per tutti");
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
