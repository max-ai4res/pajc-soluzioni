package it.unibs.pajc.redolfi.e06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Esercizio 4.6 - il server dell'ora per molti: ogni client riceve l'ora ogni secondo
 * finche' non scrive "fine" o chiude.
 *
 * Un thread virtuale per client (newVirtualThreadPerTaskExecutor). Ogni sessione ha due
 * attese, il secondo che passa e la riga che puo' arrivare: qui le unisce il timeout di
 * lettura. setSoTimeout(1000) fa scadere readLine ogni secondo con
 * SocketTimeoutException, e la sessione ne approfitta per mandare l'ora; se invece
 * arriva una riga, la guarda. Un solo thread per client e un solo punto che scrive.
 * Il contatore dei collegati e' condiviso fra le sessioni: AtomicInteger.
 *
 * Errore tipico: due thread per client (uno con sleep che scrive, uno che legge) senza
 * sincronizzare le scritture, o senza fermare il primo quando il secondo legge "fine".
 */
public class ServerOraMT {

    private static final AtomicInteger collegati = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Server dell'ora per molti sulla porta 1234");
            while (true) {
                Socket socket = server.accept();
                exec.submit(() -> servi(socket));
            }
        }
    }

    static void servi(Socket socket) {
        String chi = socket.getRemoteSocketAddress().toString();
        System.out.printf("%s entra: %d collegati%n", chi, collegati.incrementAndGet());
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            socket.setSoTimeout(1000);
            out.println("Ora ogni secondo; scrivi fine per uscire");
            while (true) {
                try {
                    String riga = in.readLine();
                    if (riga == null || riga.equalsIgnoreCase("fine")) break;
                    out.println("(scrivi fine per uscire)");
                } catch (SocketTimeoutException e) {
                    out.println(LocalTime.now().withNano(0));   // e' passato un secondo
                }
            }
        } catch (IOException e) {
            System.out.println(chi + " connessione persa: " + e.getMessage());
        } finally {
            System.out.printf("%s esce: %d collegati%n", chi, collegati.decrementAndGet());
        }
    }
}
