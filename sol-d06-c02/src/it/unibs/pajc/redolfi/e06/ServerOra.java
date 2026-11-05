package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Esercizio 2.6 - il server dell'ora: manda l'ora corrente a chi si collega, chiude e
 * torna ad aspettare.
 *
 * Il ServerSocket si apre UNA volta, fuori dal ciclo; dentro il ciclo si apre e si chiude
 * il solo Socket del client (il try-with-resources interno). Il server non legge niente:
 * parla per primo e chiude, e il client sa che la connessione finisce dopo una riga.
 * Il PrintWriter ha l'autoflush, altrimenti la riga partirebbe comunque alla chiusura,
 * ma e' una cattiva abitudine contarci.
 *
 * Errore tipico: mettere il ServerSocket nello stesso try-with-resources del Socket:
 * viene chiuso dopo il primo client e il secondo riceve "Connection refused".
 */
public class ServerOra {

    public static void main(String[] args) throws IOException {
        int porta = 1234;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm:ss");
        try (ServerSocket server = new ServerSocket(porta)) {
            System.out.println("Server dell'ora in ascolto sulla porta " + porta);
            while (true) {
                try (Socket socket = server.accept();
                     PrintWriter out = new PrintWriter(
                             socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    String ora = LocalTime.now().format(formato);
                    out.println(ora);
                    System.out.println(socket.getRemoteSocketAddress() + " <- " + ora);
                } catch (IOException e) {
                    // un client che sparisce prima della riga non ferma il server
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
