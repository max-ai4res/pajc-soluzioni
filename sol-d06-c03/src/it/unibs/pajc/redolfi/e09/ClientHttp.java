package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Il client HTTP a mano: scrive la richiesta con \r\n, legge la riga di stato, salta le
 * intestazioni fino alla riga vuota e stampa il corpo fino alla chiusura (il server ha
 * detto "Connection: close", quindi la fine del corpo e' la fine dello stream).
 * Uso: ClientHttp [host] [porta] [percorso], default localhost 1234 /ora.
 */
public class ClientHttp {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : 1234;
        String percorso = args.length > 2 ? args[2] : "/ora";
        try (Socket socket = new Socket(host, porta);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), false, StandardCharsets.US_ASCII);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8))) {
            out.print("GET " + percorso + " HTTP/1.1\r\n");
            out.print("Host: " + host + "\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.flush();                                   // niente autoflush con print
            String stato = in.readLine();
            System.out.println("stato: " + stato);
            String riga;
            while ((riga = in.readLine()) != null && !riga.isEmpty())
                ;                                          // le intestazioni
            System.out.println("corpo:");
            while ((riga = in.readLine()) != null)
                System.out.println(riga);
        }
    }
}
