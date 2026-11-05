package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

/**
 * Esercizio 2.9 - il benvenuto a piu' righe, chiuso da una riga vuota, e poi l'eco.
 *
 * Il server parla per primo e manda un numero di righe che il client non conosce in
 * anticipo: il client non puo' leggerne "tre" o "quattro", deve leggere FINO A un segnale.
 * La riga vuota e' il segnale: e' la cornice del benvenuto, come la fine riga e' la
 * cornice di ogni riga. E' lo stesso accordo che HTTP usa fra le intestazioni e il corpo.
 * Senza la riga vuota, il client leggerebbe un numero fisso di righe: una in meno (e la
 * riga avanzata gli arriverebbe come "risposta" alla prima richiesta, sfasando tutto il
 * dialogo) o una in piu' (e resterebbe bloccato in attesa di una riga che non arriva).
 *
 * Errore tipico: mandare le righe del benvenuto senza flush e con un'ultima riga NON
 * vuota: il client non ha modo di sapere che il benvenuto e' finito.
 */
public class ServerBenvenuto {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Server con benvenuto in ascolto sulla porta 1234");
            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    out.println("Server Eco di PAJC");
                    out.println("Sono le " + LocalTime.now().withNano(0));
                    out.println("Scrivi una riga e la ricevi in maiuscolo.");
                    out.println("Scrivi fine per uscire.");
                    out.println();                       // la cornice: benvenuto finito
                    String riga;
                    while ((riga = in.readLine()) != null && !riga.equals("fine"))
                        out.println(riga.toUpperCase());
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
