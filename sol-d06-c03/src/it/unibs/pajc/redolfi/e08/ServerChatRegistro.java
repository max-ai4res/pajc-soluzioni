package it.unibs.pajc.redolfi.e08;

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
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.unibs.pajc.redolfi.e08.Protocollo.Azione;

/**
 * Esercizio 3.8 - WhatsPAJC v1 con il registro dei messaggi su file e /storia n.
 *
 * Ogni messaggio a tutti viene aggiunto in coda al file del registro (res/e08/registro.txt
 * per il main; i test ne passano uno loro) nella forma "HH:mm:ss nome testo"
 * (Files.writeString con APPEND e CREATE: un'apertura per riga, che per una chat va
 * benissimo e non lascia file aperti). /storia n rilegge il file e manda le ultime n
 * righe come INFO; se il file non c'e' ancora, zero righe e nessun errore. Il protocollo
 * e' esteso con una riga nella tabella di Protocollo.COMANDI e un'azione qui (storia);
 * /aiuto e gli ERR uso la vedono da soli. Il valore di n lo controlla
 * Protocollo.interoPositivo nell'azione: "/storia tre" e "/storia -2" ricevono un ERR.
 * Un client alla volta, come nella v1.
 *
 * Errore tipico: fare Integer.parseInt(n) nell'azione senza catch: "/storia tre" chiude
 * la sessione. Un altro: scrivere il file da piu' sessioni insieme senza pensarci; qui
 * il server e' a un client alla volta, con molti client servirebbe un lock.
 */
public class ServerChatRegistro {

    static final Path REGISTRO = Path.of("res/e08/registro.txt");
    private static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Path registro;
    private final BufferedReader in;
    private final PrintWriter out;
    private String nome;                      // null finche' non arriva /nome
    private boolean attiva = true;

    ServerChatRegistro(Socket socket, Path registro) throws IOException {
        this(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8),
             new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8), registro);
    }

    /** Senza socket: in e out vengono da dove si vuole, per esempio da due stringhe. */
    ServerChatRegistro(Reader in, Writer out, Path registro) {
        this.in = new BufferedReader(in);
        this.out = new PrintWriter(out, true);
        this.registro = registro;
    }

    void invia(String riga) {
        out.println(riga);
    }

    void dialoga() throws IOException {
        invia("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome");
        String riga;
        while (attiva && (riga = in.readLine()) != null) {
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
        nome = n;
        invia("BENVENUTO " + nome);
    }

    void chi() {
        if (senzaNome()) return;
        invia("UTENTI " + nome);                                           // un client alla volta
    }

    void tutti(String testo) {
        if (senzaNome()) return;
        try {
            registra(nome, testo);
        } catch (IOException e) {
            invia("ERR registro non scrivibile: " + e.getMessage());
        }
        invia("MSG " + nome + " " + testo);
    }

    void privato(String a, String testo) {
        if (senzaNome()) return;
        if (!a.equals(nome)) { invia("ERR utente sconosciuto: " + a); return; }
        invia("PRIV " + nome + " " + a + " " + testo);
    }

    void storia(String quante) {
        if (senzaNome()) return;
        Integer n = Protocollo.interoPositivo(quante);
        if (n == null) { invia("ERR n non valido: " + quante); return; }
        try {
            List<String> righe = ultime(n);
            if (righe.isEmpty()) invia("INFO (registro vuoto)");
            righe.forEach(r -> invia("INFO " + r));
        } catch (IOException e) {
            invia("ERR registro non leggibile: " + e.getMessage());
        }
    }

    void esci() {
        invia("CIAO");
        attiva = false;                   // il ciclo di dialoga finisce
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

    // il registro su file

    void registra(String nome, String testo) throws IOException {
        if (registro.getParent() != null) Files.createDirectories(registro.getParent());
        Files.writeString(registro, LocalTime.now().format(ORA) + " " + nome + " " + testo + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    List<String> ultime(int n) throws IOException {
        if (!Files.exists(registro)) return List.of();
        List<String> tutte = Files.readAllLines(registro, StandardCharsets.UTF_8);
        return tutte.subList(Math.max(0, tutte.size() - n), tutte.size());
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        try (ServerSocket server = new ServerSocket(porta)) {
            System.out.println("WhatsPAJC v1 con registro in ascolto sulla porta " + porta);
            while (true) {
                try (Socket socket = server.accept()) {
                    new ServerChatRegistro(socket, REGISTRO).dialoga();
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
