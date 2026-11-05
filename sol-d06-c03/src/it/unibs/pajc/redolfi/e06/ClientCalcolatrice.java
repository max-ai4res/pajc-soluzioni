package it.unibs.pajc.redolfi.e06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Il client: manda la riga dell'utente cosi' com'e' (e' l'utente che scrive nel
 * protocollo) e traduce le righe del server guardando la prima parola: un numero e' un
 * risultato, ERR un errore, INFO una riga di aiuto, CIAO la fine. Lettore e scrittore
 * stanno su due thread perche' AIUTO risponde con piu' righe a una riga sola.
 */
public class ClientCalcolatrice {

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
                System.out.println("[il server ha chiuso]");
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

    /** La prima parola dice che cosa e' la riga; senza parola nota e' un risultato. */
    static String traduci(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        return switch (campi[0]) {
            case "ERR" -> "errore: " + resto;
            case "INFO" -> resto;
            case "CIAO" -> "ciao";
            default -> "= " + riga;
        };
    }
}
