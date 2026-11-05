package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Il client di console del tris: lettore e scrittore su due thread. Il lettore traduce
 * le righe del server con una tabella da parola ad azione, speculare a quella del
 * server: la prima parola e' la chiave, il resto della riga l'argomento. Tiene una copia
 * della griglia e la ridisegna a ogni MOSSA, cosi' il giocatore vede la scacchiera e non
 * le righe del protocollo. Argomenti: host e porta (localhost 1234).
 */
public class ClientTris {

    private static final char[][] griglia = new char[3][3];

    /** La tabella delle risposte: una parola del server, che cosa mostrare. */
    private static final Map<String, Consumer<String>> RISPOSTE = new HashMap<>();

    static {
        RISPOSTE.put("INFO", System.out::println);
        RISPOSTE.put("BENVENUTO", nome -> System.out.println("Ciao " + nome));
        RISPOSTE.put("PARTITA", resto -> {                      // "X Giulia"
            String[] a = Protocollo.argomenti(resto, 2);
            if (a == null) { System.out.println("? PARTITA " + resto); return; }
            for (char[] riga : griglia) Arrays.fill(riga, '.');
            System.out.println("Partita contro " + a[1] + ": sei " + a[0]);
        });
        RISPOSTE.put("TOCCA", resto -> System.out.println("Tocca a te: /m riga colonna"));
        RISPOSTE.put("MOSSA", resto -> {                        // "X 1 2"
            String[] a = Protocollo.argomenti(resto, 3);
            int r = a == null ? -1 : Protocollo.coordinata(a[1]);
            int k = a == null ? -1 : Protocollo.coordinata(a[2]);
            if (r < 0 || k < 0) { System.out.println("? MOSSA " + resto); return; }
            griglia[r][k] = a[0].charAt(0);
            disegna();
        });
        RISPOSTE.put("VINCE", simbolo -> System.out.println("Vince " + simbolo));
        RISPOSTE.put("PATTA", resto -> System.out.println("Patta"));
        RISPOSTE.put("ABBANDONO", nome -> System.out.println(nome + " ha abbandonato: hai vinto"));
        RISPOSTE.put("ERR", motivo -> System.out.println("errore: " + motivo));
        RISPOSTE.put("CIAO", resto -> System.out.println("ciao"));
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : Protocollo.PORTA;
        for (char[] r : griglia) Arrays.fill(r, '.');
        try (Socket socket = new Socket(host, porta);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true, StandardCharsets.UTF_8);
             ExecutorService exec = Executors.newFixedThreadPool(2)) {
            Future<?> lettore = exec.submit(() -> {
                String riga;
                while ((riga = in.readLine()) != null)
                    mostra(riga);
                System.out.println("[connessione chiusa]");
                return null;
            });
            exec.submit(() -> {
                BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in));
                String riga;
                while (!lettore.isDone() && (riga = tastiera.readLine()) != null)
                    out.println(riga);
                return null;
            });
            lettore.get();
        }
        System.exit(0);
    }

    /** Da riga del server a cosa mostrare: la tabella decide, la parola ignota si stampa com'e'. */
    static void mostra(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        RISPOSTE.getOrDefault(campi[0], r -> System.out.println("? " + riga)).accept(resto);
    }

    static void disegna() {
        for (char[] riga : griglia) System.out.println(" " + riga[0] + " " + riga[1] + " " + riga[2]);
    }
}
