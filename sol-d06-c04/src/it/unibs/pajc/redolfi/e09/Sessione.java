package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import it.unibs.pajc.redolfi.e09.Protocollo.Azione;

/**
 * Una sessione del tris: il nome, la partita in corso (o null se libera) e il dialogo,
 * che interpreta le righe con la tabella del Protocollo. In produzione nasce da un
 * Socket; nei test da un Reader e un Writer (due stringhe), senza rete, e allora anche
 * run() si puo' chiamare direttamente: il finally, che fa vincere l'avversario a
 * tavolino, e' lo stesso.
 */
public class Sessione implements Runnable {

    private final Socket socket;              // null senza rete
    private final Arbitro arbitro;            // lo stato condiviso: la coda dei liberi
    private BufferedReader in;
    private PrintWriter out;
    private volatile String nome;             // null finche' non arriva /nome
    private volatile Partita partita;         // null se libera; la scrive anche l'avversario
    private boolean attiva = true;

    public Sessione(Socket socket, Arbitro arbitro) {
        this.socket = socket;
        this.arbitro = arbitro;
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    public Sessione(Reader in, Writer out, Arbitro arbitro) {
        this(null, arbitro);
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    public String nome() {
        return nome;
    }

    void partita(Partita p) {
        partita = p;
    }

    /** L'unico punto da cui si scrive al client: l'arbitro e la partita non mescolano le righe. */
    public synchronized void invia(String riga) {
        out.println(riga);
    }

    @Override
    public void run() {
        try {
            if (socket != null) {
                log("collegato da " + socket.getRemoteSocketAddress());
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            }
            dialoga();
        } catch (IOException e) {                                 // informazione, non un bug
            log("connessione persa: " + e.getMessage());
        } finally {                                               // in TUTTI i modi di finire
            lascia();
            chiudi();
            log("chiuso");
        }
    }

    /** Il dialogo: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Tris in rete. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) {      // null: il client ha chiuso
            elabora(riga);
        }
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
        nome = n;
        invia("BENVENUTO " + nome);
        arbitro.accoppia(this);                                            // in coda, o in partita
    }

    /** La mossa va alla partita, che ha il lock: e' lei a dire se e' il turno e la casella e' libera. */
    void mossa(String r, String c) {
        if (senzaNome()) return;
        int riga = Protocollo.coordinata(r), colonna = Protocollo.coordinata(c);
        if (riga < 0 || colonna < 0) { invia("ERR riga e colonna vanno da 0 a 2"); return; }
        Partita p = partita;
        if (p == null) { invia("ERR non sei in una partita"); return; }
        p.mossa(this, riga, colonna);
        if (p.finita()) {                                                  // entrambi tornano liberi
            partita = null;
            Sessione altro = p.avversarioDi(this);
            altro.partita(null);
            arbitro.accoppia(this);
            arbitro.accoppia(altro);
        }
    }

    void esci() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce, poi il finally
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
    }

    private boolean senzaNome() {
        if (nome != null) return false;
        invia("ERR prima scegli un nome con /nome");
        return true;
    }

    /** Chi se ne va a meta' partita (anche staccando) la perde: l'avversario vince e torna libero. */
    private void lascia() {
        Partita p = partita;
        if (p != null && !p.finita()) {
            p.abbandona(this);
            Sessione altro = p.avversarioDi(this);
            altro.partita(null);
            arbitro.accoppia(altro);
        }
        arbitro.togli(this);
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
