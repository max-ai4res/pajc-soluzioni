package it.unibs.pajc.redolfi.e01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Il client che si collega dieci volte INSIEME (dieci thread) e misura il tempo totale:
 * con ServerOraUno circa 5 s, con ServerOraMolti circa 0,5 s. Dieci connessioni in fila
 * da un solo thread darebbero 5 s in entrambi i casi: la misura ha senso solo se i
 * client sono davvero contemporanei.
 */
public class ClientDieciVolte {

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        long t0 = System.nanoTime();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> ore = new ArrayList<>();
            for (int i = 0; i < 10; i++) ore.add(exec.submit(() -> leggiOra(host)));
            for (int i = 0; i < 10; i++) System.out.println("client " + i + ": " + ore.get(i).get());
        }
        System.out.printf("tempo totale: %.2f s%n", (System.nanoTime() - t0) / 1e9);
    }

    static String leggiOra(String host) throws IOException {
        try (Socket s = new Socket(host, 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
            String ora = in.readLine();
            return ora == null ? "(nessuna risposta)" : ora;
        }
    }
}
