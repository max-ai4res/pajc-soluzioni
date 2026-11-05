package it.unibs.pajc.redolfi.e02;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Il client di console con il lettore su un thread: stampa tutto, compreso il saluto di chiusura, e termina quando il server chiude. */
public class ClientEcoRovescio {

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        try (Socket socket = new Socket(host, 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             ExecutorService exec = Executors.newFixedThreadPool(2)) {
            Future<?> lettore = exec.submit(() -> {
                String riga;
                while ((riga = in.readLine()) != null) System.out.println("> " + riga);
                System.out.println("[il server ha chiuso]");
                return null;
            });
            exec.submit(() -> {
                BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in));
                String riga;
                while (!lettore.isDone() && (riga = tastiera.readLine()) != null) out.println(riga);
                return null;
            });
            lettore.get();
        }
        System.exit(0);
    }
}
