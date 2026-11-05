package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Esercizio 2.7 - eco con contatore: ogni riga torna con il suo numero d'ordine e in
 * maiuscolo; "fine" chiude con il totale.
 *
 * Il contatore e' lo stato del dialogo, e vive per la durata di UNA connessione: sta in
 * una variabile locale dentro il try, non in un campo statico, cosi' un secondo client
 * (dopo il primo) riparte da 1. "fine" non viene contata: il totale e' quello delle righe
 * di testo. Il server resta in ascolto per il client successivo.
 *
 * Errore tipico: controllare "fine" DOPO aver risposto con il numero, cosi' il client
 * riceve "4: FINE" e poi il totale, e non sa quale delle due e' l'ultima riga.
 */
public class ServerEcoContatore {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Eco con contatore in ascolto sulla porta 1234");
            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    int n = 0;
                    String riga;
                    while ((riga = in.readLine()) != null) {
                        if (riga.equals("fine")) break;
                        n++;
                        out.println(n + ": " + riga.toUpperCase());
                    }
                    out.println("Totale: " + n + " righe. Ciao!");
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
