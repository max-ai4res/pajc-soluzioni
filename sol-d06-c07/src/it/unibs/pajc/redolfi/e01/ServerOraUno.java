package it.unibs.pajc.redolfi.e01;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

/**
 * Palestra 7.1 (a) - il server dell'ora, un client alla volta: il tema d'esame nella
 * versione base. Prima di rispondere aspetta mezzo secondo (un lavoro simulato), cosi'
 * la differenza con la versione (b) si misura: dieci client in fila = cinque secondi.
 *
 * Errore tipico: il ServerSocket nel try-with-resources del client: chiuso dopo il primo.
 */
public class ServerOraUno {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Ora (un client alla volta) sulla porta 1234");
            while (true) {
                try (Socket socket = server.accept();
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    Thread.sleep(500);                       // il "lavoro"
                    out.println(LocalTime.now().withNano(0));
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
