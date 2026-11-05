package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
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
 * Il client del quiz: lettore e scrittore su due thread (il server parla anche da solo,
 * per esempio la domanda dopo la risposta), e il lettore traduce ogni riga in italiano
 * con una tabella speculare a quella del server: la prima parola e' la chiave, il resto
 * della riga l'argomento.
 */
public class ClientQuiz {

    /** Da parola del server a testo per l'utente. */
    private static final Map<String, Function<String, String>> TRADUZIONI = new HashMap<>();

    static {
        TRADUZIONI.put("INFO", testo -> testo);
        TRADUZIONI.put("BENVENUTO", nome -> "Benvenuto, " + nome + "! Rispondi con /r testo");
        TRADUZIONI.put("DOMANDA", resto -> {
            String[] d = resto.split(" ", 2);                      // n e testo
            return "Domanda " + d[0] + ": " + (d.length > 1 ? d[1] : "");
        });
        TRADUZIONI.put("OK", punti -> "Giusto! Punti: " + punti);
        TRADUZIONI.put("NO", giusta -> "No, la risposta era: " + giusta);
        TRADUZIONI.put("PUNTI", punti -> "Punti finora: " + punti);
        TRADUZIONI.put("FINE", punti -> "Fine: " + punti + " punti");
        TRADUZIONI.put("ERR", motivo -> "errore: " + motivo);
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : Protocollo.PORTA;
        try (Socket socket = new Socket(host, porta);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true, StandardCharsets.UTF_8);
             ExecutorService exec = Executors.newFixedThreadPool(2)) {
            Future<?> lettore = exec.submit(() -> {
                String riga;
                while ((riga = in.readLine()) != null)
                    System.out.println(traduci(riga));
                System.out.println("[fine del quiz, connessione chiusa]");
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

    /** Una parola che non e' in tabella si mostra com'e', con un punto di domanda. */
    static String traduci(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        Function<String, String> t = TRADUZIONI.get(campi[0]);
        return t == null ? "? " + riga : t.apply(resto);
    }
}
