package it.unibs.pajc.redolfi.e06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e06.Protocollo.Azione;

/**
 * Il dialogo con un client della calcolatrice: legge le righe, le interpreta con la
 * tabella del Protocollo, risponde. In produzione nasce da un Socket; nei test da un
 * Reader e un Writer qualsiasi (due stringhe), senza rete.
 */
public class Sessione {

    private final BufferedReader in;
    private final PrintWriter out;
    private boolean attiva = true;

    public Sessione(Socket socket) throws IOException {
        this(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8),
             new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8));
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out) {
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    void invia(String riga) {
        out.println(riga);
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
        if (riga.isBlank()) return;
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
    }

    void dividi(long a, long b) {
        if (b == 0) { invia("ERR divisione per zero"); return; }
        risultato(a / b);
    }

    void fine() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
    }
}
