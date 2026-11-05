package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Il client dell'eco con contatore: a turno, una riga mandata e una letta. Dopo "fine"
 * legge ancora UNA riga (il totale) e poi chiude: e' l'accordo con il server, e il client
 * deve conoscerlo. Un readLine che restituisce null (server sparito) fa uscire.
 */
public class ClientEcoContatore {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        try (Socket socket = new Socket(host, 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true, StandardCharsets.UTF_8);
             BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Collegato a " + socket.getRemoteSocketAddress()
                    + " (fine per uscire)");
            String riga;
            while ((riga = tastiera.readLine()) != null) {
                out.println(riga);
                String risposta = in.readLine();
                if (risposta == null) {
                    System.out.println("il server ha chiuso");
                    return;
                }
                System.out.println("> " + risposta);
                if (riga.equals("fine")) return;
            }
        }
    }
}
