package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Il client: legge il benvenuto riga per riga finche' non trova la riga vuota (o null,
 * se il server chiude prima), lo stampa, e solo allora comincia il dialogo a turno.
 * Le due fasi hanno due cicli diversi perche' hanno due accordi diversi: "leggi fino alla
 * riga vuota" e "una risposta per ogni richiesta".
 */
public class ClientBenvenuto {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        try (Socket socket = new Socket(host, 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true, StandardCharsets.UTF_8);
             BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in))) {
            // fase 1: il benvenuto, fino alla riga vuota
            String riga;
            while ((riga = in.readLine()) != null && !riga.isEmpty())
                System.out.println("| " + riga);
            if (riga == null) {
                System.out.println("il server ha chiuso durante il benvenuto");
                return;
            }
            // fase 2: il dialogo a turno
            while ((riga = tastiera.readLine()) != null) {
                out.println(riga);
                if (riga.equals("fine")) return;
                String risposta = in.readLine();
                if (risposta == null) {
                    System.out.println("il server ha chiuso");
                    return;
                }
                System.out.println("> " + risposta);
            }
        }
    }
}
