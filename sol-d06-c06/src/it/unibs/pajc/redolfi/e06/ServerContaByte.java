package it.unibs.pajc.redolfi.e06;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 6.6 - il contatore di byte: riceve cornici (lunghezza davanti) e risponde a
 * ciascuna con una riga writeUTF; una cornice vuota chiude.
 *
 * Tutto il socket e' a byte: Data stream nei due versi, nessun BufferedReader. Una
 * cornice puo' contenere qualunque byte, anche 10 (l'a capo): la cornice non lo guarda,
 * conta. readFully aspetta tutti i byte dichiarati. Un thread virtuale per client.
 *
 * Errore tipico: leggere la cornice con read(buf) una volta sola e contare quello che e'
 * tornato: su un socket arriva a pezzi, e il conteggio e' sbagliato "quasi sempre".
 */
public class ServerContaByte {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Conta byte sulla porta 1234");
            while (true) {
                Socket socket = server.accept();
                exec.submit(() -> servi(socket));
            }
        }
    }

    static void servi(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            long totale = 0;
            while (true) {
                byte[] dati = Cornice.leggi(in);
                if (dati.length == 0) {
                    out.writeUTF("ciao: " + totale + " byte in tutto");
                    out.flush();
                    return;
                }
                totale += dati.length;
                out.writeUTF("ricevuti " + dati.length + " byte, " + totale + " in totale");
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("client perso: " + e.getMessage());
        }
    }
}
