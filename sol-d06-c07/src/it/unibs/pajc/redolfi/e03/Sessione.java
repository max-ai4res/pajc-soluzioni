package it.unibs.pajc.redolfi.e03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e03.Protocollo.Azione;

/**
 * Il dialogo con un client della calcolatrice: legge le righe, le interpreta con la
 * tabella del Protocollo, risponde; ogni risultato conta nelle Statistiche condivise.
 * Un Runnable, un thread per sessione. In produzione nasce da un Socket; nei test da un
 * Reader e un Writer qualsiasi (due stringhe), senza rete.
 */
public class Sessione implements Runnable {

    private final Socket socket;              // null senza rete
    private final Statistiche statistiche;
    private BufferedReader in;
    private PrintWriter out;
    private boolean attiva = true;

    public Sessione(Socket socket, Statistiche statistiche) {
        this.socket = socket;
        this.statistiche = statistiche;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Statistiche statistiche) {
        this(null, statistiche);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    void invia(String riga) {
        out.println(riga);
    }

    @Override
    public void run() {
        statistiche.entra();
        try (socket) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (IOException e) {
            System.err.println("client perso: " + e.getMessage());
        } finally {
            statistiche.esce();
        }
    }

    /** Il dialogo: una risposta per ogni riga, finche' il client chiude o manda FINE. */
    void dialoga() throws IOException {
        invia("INFO Calcolatrice remota: ADD 2 3, AIUTO per l'elenco, FINE per uscire");
        String riga;
        while (attiva && (riga = in.readLine()) != null) {
            elabora(riga);
        }
    }

    /** Da riga a operazione: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) { invia("ERR riga vuota"); return; }
        String[] campi = riga.strip().split(" ", 2);                       // l'operazione e il resto
        Azione a = Protocollo.COMANDI.get(campi[0].toUpperCase());
        if (a == null) { invia("ERR operazione sconosciuta: " + campi[0]); return; }
        String[] arg = Protocollo.argomenti(campi.length > 1 ? campi[1] : "", a.operandi());
        if (arg == null) { invia("ERR uso: " + a.aiuto()); return; }       // forma sbagliata
        long[] operandi = Protocollo.numeri(arg);
        if (operandi == null) { invia("ERR operando non numerico: " + String.join(" ", arg)); return; }
        a.esegui().accept(this, operandi);
    }

    // le azioni della tabella

    void risultato(long valore) {
        invia(String.valueOf(valore));
        statistiche.operazione();
        System.out.printf("[%s] -> %d (operazioni: %d)%n",
                socket != null ? socket.getRemoteSocketAddress() : "test", valore, statistiche.operazioni());
    }

    void dividi(long a, long b) {
        if (b == 0) { invia("ERR divisione per zero"); return; }
        risultato(a / b);
    }

    void stat() {
        invia("STAT " + statistiche.operazioni() + " " + statistiche.collegati());
    }

    void fine() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
    }
}
