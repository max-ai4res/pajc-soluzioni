package it.unibs.pajc.redolfi.e06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Il client del server dell'ora: si collega, legge una riga, la stampa e termina.
 * Non scrive nulla: il protocollo (una riga dal server, poi chiusura) lo decide il server.
 * Se il server chiude prima di mandare la riga, readLine restituisce null e lo diciamo.
 */
public class ClientOra {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        try (Socket socket = new Socket(host, 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8))) {
            String ora = in.readLine();
            System.out.println(ora == null ? "il server ha chiuso senza rispondere"
                                           : "ora del server: " + ora);
        }
    }
}
