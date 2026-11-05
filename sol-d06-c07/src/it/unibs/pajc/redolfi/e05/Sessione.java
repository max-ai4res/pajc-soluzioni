package it.unibs.pajc.redolfi.e05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e05.Protocollo.Azione;

/**
 * Tutto cio' che riguarda un client: legge le sue righe, le interpreta con la tabella
 * del Protocollo, gli scrive le risposte. Oltre alla chat tiene l'ultima offerta di file
 * ricevuta, in attesa di /accetta. In produzione nasce da un Socket; nei test da un
 * Reader e un Writer (due stringhe).
 */
public class Sessione implements Runnable {

    private static final int TIMEOUT_MS = 30 * 60 * 1000;   // mezz'ora di silenzio, poi lancia

    private final Socket socket;                    // null senza rete
    private final Registro registro;
    private final Deposito deposito;
    private volatile Deposito.Offerta offerta;      // l'ultimo file offerto a me, non ancora accettato
    private BufferedReader in;
    private PrintWriter out;
    private String nome;                            // null finche' non arriva /nome
    private boolean attiva = true;

    public Sessione(Socket socket, Registro registro, Deposito deposito) {
        this.socket = socket;
        this.registro = registro;
        this.deposito = deposito;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Registro registro, Deposito deposito) {
        this(null, registro, deposito);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    public String nome() { return nome; }

    /** Il Deposito avvisa la sessione del destinatario: questo file e' per te. */
    void offri(Deposito.Offerta o) { offerta = o; }

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
        } catch (SocketTimeoutException e) {
            invia("ERR inattivo da troppo tempo");
        } catch (IOException e) {
            log("connessione persa: " + e.getMessage());
        } finally {                                               // in TUTTI i modi di finire
            if (nome != null) {
                registro.rimuovi(nome);
                registro.aTutti("ESCE " + nome);
            }
            chiudi();
            log("chiuso");
        }
    }

    /** Il dialogo: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) elabora(riga);
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
        invia("BENVENUTO " + nome);
        registro.aAltri("ENTRA " + nome, this);
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
        dest.invia(riga);
        if (dest != this) invia(riga);
    }

    /** /invia N f d: un'offerta nel deposito, CARICA id a me, FILE al destinatario. */
    void inviaFile(String a, String nomeFile, String dim) {
        if (senzaNome()) return;
        long dimensione = Protocollo.numero(dim);
        if (dimensione < 0) { invia("ERR dimensione non valida: " + dim); return; }
        Sessione dest = registro.trova(a);
        if (dest == null) { invia("ERR utente sconosciuto: " + a); return; }
        Deposito.Offerta o = deposito.offri(nome, dest, nomeFile, dimensione);
        invia("CARICA " + o.id());                                          // il mittente carica i byte
        dest.offri(o);
        dest.invia("FILE " + nome + " " + nomeFile + " " + dimensione + " " + o.id());
    }

    /** /accetta: il destinatario prende i byte dal deposito con l'id dell'offerta. */
    void accetta() {
        if (senzaNome()) return;
        Deposito.Offerta o = offerta;
        if (o == null) { invia("ERR nessun file da accettare"); return; }
        offerta = null;
        invia("PRENDI " + o.id() + " " + o.nome());
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

    private void log(String testo) {
        System.out.printf("[%s] %s%n", nome != null ? nome : socket.getRemoteSocketAddress(), testo);
    }
}
