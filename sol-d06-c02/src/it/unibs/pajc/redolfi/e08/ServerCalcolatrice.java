package it.unibs.pajc.redolfi.e08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Esercizio 2.8 - la calcolatrice remota: "ADD 2 3" -> "5", "DIV 1 0" -> "ERR ...".
 *
 * Il punto e' che il server non muore MAI per una riga sbagliata: ogni riga viene
 * tradotta in una risposta da un metodo a parte, rispondi(), che restituisce sempre una
 * stringa, e tutto cio' che puo' andare storto (pochi campi, numero non valido,
 * operazione ignota, divisione per zero) diventa un "ERR motivo". Il ciclo di lettura non
 * vede mai un'eccezione. Il metodo e' statico e puro: si puo' provare senza rete.
 *
 * Errore tipico: lasciare che NumberFormatException esca dal ciclo: il thread chiude la
 * connessione e il client riceve null al posto dell'ERR.
 */
public class ServerCalcolatrice {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Calcolatrice remota in ascolto sulla porta 1234");
            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    String riga;
                    while ((riga = in.readLine()) != null) {
                        if (riga.equalsIgnoreCase("fine")) break;
                        out.println(rispondi(riga));
                    }
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }

    /** Traduce una riga di richiesta in una riga di risposta, senza mai lanciare. */
    static String rispondi(String riga) {
        String[] campi = riga.trim().split("\\s+");
        if (campi.length != 3)
            return "ERR servono operazione e due numeri, es. ADD 2 3";
        long a, b;
        try {
            a = Long.parseLong(campi[1]);
            b = Long.parseLong(campi[2]);
        } catch (NumberFormatException e) {
            return "ERR numero non valido: " + e.getMessage();
        }
        return switch (campi[0].toUpperCase()) {
            case "ADD" -> String.valueOf(a + b);
            case "SUB" -> String.valueOf(a - b);
            case "MUL" -> String.valueOf(a * b);
            case "DIV" -> b == 0 ? "ERR divisione per zero" : String.valueOf(a / b);
            default -> "ERR operazione sconosciuta: " + campi[0];
        };
    }
}
