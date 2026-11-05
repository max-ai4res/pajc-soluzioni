package it.unibs.pajc.redolfi.e02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.2 - eco a rovescio con scadenza: ogni riga torna rovesciata; dieci secondi
 * di silenzio e il server saluta ("troppo lento, ciao") e chiude. Il log dice come e'
 * finita ogni sessione: fine, chiusura del client (null), scadenza, connessione persa.
 *
 * Il catch della SocketTimeoutException sta DENTRO il ciclo, attorno al solo readLine:
 * un catch fuori dal try-with-resources arriverebbe a socket gia' chiuso (le risorse si
 * chiudono prima dei catch) e il saluto non partirebbe. Per la connessione persa
 * (IOException fuori) non c'e' nessuno a cui scrivere.
 */
public class ServerEcoRovescio {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Eco a rovescio sulla porta 1234 (10 s di scadenza)");
            while (true) {
                Socket socket = server.accept();
                exec.submit(() -> servi(socket));
            }
        }
    }

    static void servi(Socket socket) {
        String chi = socket.getRemoteSocketAddress().toString();
        System.out.println(chi + " entra");
        String motivo = "connessione persa";
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            socket.setSoTimeout(10_000);
            while (true) {
                String riga;
                try {
                    riga = in.readLine();
                } catch (SocketTimeoutException e) {        // dentro il ciclo: out e' ancora aperto
                    out.println("troppo lento, ciao");
                    motivo = "scadenza";
                    return;
                }
                if (riga == null) { motivo = "il client ha chiuso"; return; }
                if (riga.equals("fine")) { out.println("ciao"); motivo = "fine"; return; }
                out.println(new StringBuilder(riga).reverse());
            }
        } catch (IOException e) {
            motivo = "connessione persa: " + e.getMessage();
        } finally {
            System.out.println(chi + " esce: " + motivo);
        }
    }
}
