package it.unibs.pajc.redolfi.e04;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Il client di console del quiz a piu' giocatori: lettore su un thread, le righe del
 * server tradotte in italiano con una tabella da parola a traduzione, speculare a quella
 * dei comandi sul server; una parola sconosciuta si stampa com'e' con un punto di
 * domanda. Cio' che si scrive parte com'e': la tabella dei comandi ce l'ha il server.
 *
 * Uso: ClientQuizMulti [host] [porta]
 */
public class ClientQuizMulti {

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private static final Map<String, Function<String, String>> RISPOSTE = new HashMap<>();

    static {
        RISPOSTE.put("INFO", testo -> testo);
        RISPOSTE.put("BENVENUTO", nome -> "Benvenuto " + nome + ": rispondi con /r testo");
        RISPOSTE.put("DOMANDA", resto -> {
            String[] a = Protocollo.argomenti(resto, 2);                 // numero e testo
            return a == null ? "? " + resto : "Domanda " + a[0] + ": " + a[1];
        });
        RISPOSTE.put("PUNTO", resto -> {
            String[] a = Protocollo.argomenti(resto, 2);                 // nome e risposta giusta
            return a == null ? "? " + resto : "Punto a " + a[0] + " (risposta: " + a[1] + ")";
        });
        RISPOSTE.put("TEMPO", risposta -> "Tempo scaduto, nessuno: era " + risposta);
        RISPOSTE.put("NO", resto -> "No, riprova");
        RISPOSTE.put("CLASSIFICA", punti -> "Classifica: " + Protocollo.classifica(punti));
        RISPOSTE.put("FINE", punti -> "Fine! Classifica finale: " + Protocollo.classifica(punti));
        RISPOSTE.put("ERR", motivo -> "errore: " + motivo);
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : Protocollo.PORTA;
        try (Socket socket = new Socket(host, porta);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             ExecutorService exec = Executors.newFixedThreadPool(2)) {
            Future<?> lettore = exec.submit(() -> {
                String riga;
                while ((riga = in.readLine()) != null) System.out.println(traduci(riga));
                System.out.println("[connessione chiusa]");
                return null;
            });
            exec.submit(() -> {
                BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in));
                String riga;
                while (!lettore.isDone() && (riga = tastiera.readLine()) != null) out.println(riga);
                return null;
            });
            lettore.get();
        }
        System.exit(0);
    }

    /** La tabella speculare: la parola in testa sceglie la traduzione, il resto e' l'argomento. */
    static String traduci(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        return RISPOSTE.getOrDefault(campi[0], r -> "? " + riga).apply(resto);
    }
}
