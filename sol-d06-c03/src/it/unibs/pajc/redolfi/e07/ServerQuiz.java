package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import it.unibs.pajc.redolfi.e07.Protocollo.Azione;

/**
 * Esercizio 3.7 - il quiz: un client alla volta, le domande da res/e07/domande.txt
 * (una per riga, "domanda|risposta").
 *
 * Il protocollo e' la tabella di Protocollo.COMANDI; elabora cerca ogni riga nella
 * tabella, controlla gli argomenti ed esegue l'azione, un metodo di questa classe. Il
 * dialogo e' a passo guidato dal SERVER, e lo stato del dialogo sta in due campi: il
 * nome (null finche' non arriva /nome) e corrente, l'indice della domanda aperta, cioe'
 * "in attesa della risposta alla domanda corrente + 1". Le azioni consultano lo stato:
 * risposta confronta con la domanda aperta e passa alla seguente, classifica legge i
 * punti e non tocca corrente. Dopo l'ultima domanda il server manda FINE e chiude. Le
 * risposte si confrontano senza distinguere maiuscole e spazi ai bordi.
 *
 * Errore tipico: il ciclo "manda domanda, leggi UNA riga, confronta", che tratta
 * /classifica come risposta sbagliata e sfasa il quiz. Con la tabella ogni riga va
 * all'azione del suo comando, e solo /r tocca la domanda aperta.
 */
public class ServerQuiz {

    record Quesito(String domanda, String risposta) {}

    private final List<Quesito> quesiti;
    private final BufferedReader in;
    private final PrintWriter out;
    private String nome;        // null: aspetto /nome
    private int corrente;       // indice della domanda aperta
    private int punti;
    private boolean attiva = true;

    ServerQuiz(List<Quesito> quesiti, Socket socket) throws IOException {
        this(quesiti, new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8),
             new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8));
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    ServerQuiz(List<Quesito> quesiti, Reader in, Writer out) {
        this.quesiti = quesiti;
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
    }

    void invia(String riga) {
        out.println(riga);
    }

    void dialoga() throws IOException {
        invia("INFO Quiz di PAJC: " + quesiti.size() + " domande. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) {
            elabora(riga);
        }
    }

    /** Da riga a comando: la tabella decide. Non lancia mai: al massimo risponde ERR. */
    void elabora(String riga) {
        if (riga.isBlank()) return;
        String[] campi = riga.strip().split(" ", 2);                       // il comando e il resto
        Azione a = Protocollo.COMANDI.get(campi[0].toLowerCase());
        if (a == null) { invia("ERR comando sconosciuto: " + campi[0] + " (i comandi cominciano con /)"); return; }
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
        domanda();
    }

    void risposta(String testo) {
        if (senzaNome()) return;
        Quesito q = quesiti.get(corrente);                                 // la domanda aperta
        if (testo.strip().equalsIgnoreCase(q.risposta())) {
            punti++;
            invia("OK " + punti);
        } else {
            invia("NO " + q.risposta());
        }
        corrente++;
        domanda();
    }

    void classifica() {
        if (senzaNome()) return;
        invia("PUNTI " + punti);
    }

    void esci() {
        invia("FINE " + punti);
        attiva = false;                   // il ciclo di dialoga finisce
    }

    void aiuto() {
        for (Azione a : Protocollo.COMANDI.values()) invia("INFO " + a.aiuto());
    }

    /** La domanda aperta, o FINE se non ce ne sono piu'. */
    private void domanda() {
        if (corrente == quesiti.size()) { esci(); return; }
        invia("DOMANDA " + (corrente + 1) + " " + quesiti.get(corrente).domanda());
    }

    private boolean senzaNome() {
        if (nome != null) return false;
        invia("ERR prima scegli un nome con /nome");
        return true;
    }

    static List<Quesito> leggi(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .filter(l -> l.contains("|"))
                .map(l -> new Quesito(l.substring(0, l.indexOf('|')).strip(),
                                      l.substring(l.indexOf('|') + 1).strip()))
                .toList();
    }

    public static void main(String[] args) throws IOException {
        List<Quesito> quesiti = leggi(Path.of(args.length > 0 ? args[0] : "res/e07/domande.txt"));
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : Protocollo.PORTA;
        try (ServerSocket server = new ServerSocket(porta)) {
            System.out.println("Quiz in ascolto sulla porta " + porta + ", " + quesiti.size() + " domande");
            while (true) {
                try (Socket socket = server.accept()) {
                    new ServerQuiz(quesiti, socket).dialoga();
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
