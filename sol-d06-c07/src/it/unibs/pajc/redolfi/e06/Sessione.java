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
 * Una sessione del tris: legge le righe del client, le interpreta con la tabella del
 * Protocollo, gli scrive le risposte; tiene il nome, la partita in corso (o null) e la
 * partita guardata, se spettatore. In produzione nasce da un Socket; nei test da un
 * Reader e un Writer (due stringhe).
 */
public class Sessione implements Runnable {
    private final Socket socket;               // null senza rete
    private final Arbitro arbitro;
    private BufferedReader in;
    private PrintWriter out;
    private volatile String nome;              // null finche' non arriva /nome
    private volatile Partita partita;          // null se libera
    private volatile Partita guardata;         // la partita che sto guardando, se spettatore
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

    public String nome() { return nome; }
    void partita(Partita p) { partita = p; }

    /** synchronized: le righe della Partita arrivano anche dal thread dell'avversario. */
    public synchronized void invia(String riga) { out.println(riga); }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            dialoga();
        } catch (IOException e) {
            System.out.println(nome + ": connessione persa");
        } finally {
            if (guardata != null) guardata.smettiDiGuardare(this);
            Partita p = partita;
            if (p != null && !p.finita()) {         // l'avversario vince e torna libero
                p.abbandona(this);
                arbitro.finita(p);
                Sessione altro = p.avversarioDi(this);
                altro.partita(null);
                arbitro.accoppia(altro);
            }
            arbitro.togli(this);
            try { socket.close(); } catch (IOException e) { /* gia' chiuso */ }
        }
    }

    /** Il dialogo: nei test si chiama direttamente, su due stringhe. */
    void dialoga() throws IOException {
        invia("INFO Tris in rete. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) elabora(riga);
    }

    /** Da riga a comando: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) return;
        String[] campi = riga.strip().split(" ", 2);                       // il comando e il resto
        Azione a = Protocollo.COMANDI.get(campi[0].toLowerCase());
        if (a == null) { invia("ERR comando sconosciuto: " + campi[0]); return; }
        String resto = campi.length > 1 ? campi[1] : "";
        String[] arg = a.facoltativi() && resto.isBlank() ? new String[0]
                     : Protocollo.argomenti(resto, a.argomenti());
        if (arg == null) { invia("ERR uso: " + a.aiuto()); return; }
        a.esegui().accept(this, arg);
    }

    // le azioni della tabella

    void nome(String n) {
        if (nome != null) { invia("ERR hai gia' un nome: " + nome); return; }
        if (!Protocollo.nomeValido(n)) { invia("ERR nome non valido: " + n); return; }
        nome = n;
        invia("BENVENUTO " + nome);
        arbitro.accoppia(this);
    }

    void mossa(String riga, String colonna) {
        if (senzaNome()) return;
        int r = Protocollo.coordinata(riga), k = Protocollo.coordinata(colonna);
        if (r < 0 || k < 0) { invia("ERR riga e colonna vanno da 0 a 2"); return; }
        Partita p = partita;
        if (p == null) { invia("ERR non sei in una partita"); return; }
        p.mossa(this, r, k);
        if (p.finita()) {                       // partita chiusa: entrambi tornano liberi
            arbitro.finita(p);
            partita = null;
            Sessione altro = p.avversarioDi(this);
            altro.partita(null);
            arbitro.accoppia(this);
            arbitro.accoppia(altro);
        }
    }

    /** /guarda senza argomento: l'elenco; con il nome di una partita: la si segue. */
    void guarda(String quale) {
        if (senzaNome()) return;
        if (partita != null) { invia("ERR stai giocando"); return; }
        arbitro.togli(this);                                  // uno spettatore non e' in coda
        if (guardata != null) { guardata.smettiDiGuardare(this); guardata = null; }
        if (quale == null) { invia("PARTITE " + String.join(",", arbitro.partite())); return; }
        Partita p = arbitro.partita(quale);
        if (p == null) { invia("ERR partita sconosciuta: " + quale); return; }
        guardata = p;
        p.guarda(this);
    }

    void esci() {
        invia("CIAO");
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
