package it.unibs.pajc.redolfi.e01;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.1 (b) - il server dell'ora con un thread (virtuale) per client: dieci
 * client insieme sono serviti insieme, e il client di prova misura mezzo secondo invece
 * di cinque. Il lavoro di ogni client (sleep + println + close) sta in servi(), eseguito
 * dall'executor: il thread di accept non aspetta mai nessuno.
 */
public class ServerOraMolti {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Ora (un thread per client) sulla porta 1234");
            while (true) {
                Socket socket = server.accept();
                exec.submit(() -> servi(socket));
            }
        }
    }

    static void servi(Socket socket) {
        try (socket;
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            Thread.sleep(500);
            out.println(LocalTime.now().withNano(0));
        } catch (IOException e) {
            System.err.println("client perso: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
