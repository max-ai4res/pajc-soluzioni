package it.unibs.pajc.redolfi.e04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e04.Protocollo.Azione;

/**
 * La sessione di un giocatore: legge le sue righe, le interpreta con la tabella del
 * Protocollo, gli scrive le risposte; lo stato del quiz sta nell'oggetto Quiz condiviso.
 * In produzione nasce da un Socket; nei test da un Reader e un Writer (due stringhe).
 */
public class Sessione implements Runnable {
    private final Socket socket;              // null senza rete
    private final Quiz quiz;
    private BufferedReader in;
    private PrintWriter out;
    private String nome;                      // null finche' non arriva /nome
    private boolean attiva = true;

    public Sessione(Socket socket, Quiz quiz) {
        this.socket = socket;
        this.quiz = quiz;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Quiz quiz) {
        this(null, quiz);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    public String nome() { return nome; }

    /** synchronized: le righe del Quiz arrivano dai thread degli altri giocatori e dal timer. */
    public synchronized void invia(String riga) { out.println(riga); }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (IOException e) {
            System.out.println((nome != null ? nome : "?") + ": connessione persa");
        } finally {
            if (nome != null) quiz.rimuovi(nome);
            try { socket.close(); } catch (IOException e) { /* gia' chiuso */ }
        }
    }

    /** Il dialogo: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Quiz a piu' giocatori. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) elabora(riga);
    }

    /** Da riga a comando: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) return;
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
        if (!quiz.aggiungi(n, this)) { invia("ERR nome gia' in uso: " + n); return; }
        nome = n;
        invia("BENVENUTO " + nome);
    }

    void risposta(String testo) {
        if (senzaNome()) return;
        quiz.risposta(nome, this, testo);
    }

    void classifica() {
        if (senzaNome()) return;
        invia("CLASSIFICA " + Protocollo.classifica(quiz.classifica()));
    }

    void esci() {
        attiva = false;                       // il ciclo di dialoga finisce, poi il finally
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
    }

    private boolean senzaNome() {
        if (nome != null) return false;
        invia("ERR prima scegli un nome con /nome");
        return true;
    }
}
