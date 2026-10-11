package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Esercizio 6.6: le parole di molti file.
 *
 * Un compito per file: legge tutto il file (fuori da ogni lock), lo divide in parole e
 * per ciascuna fa merge(parola, 1, Integer::sum) su un'unica ConcurrentHashMap. Il merge
 * e' atomico per chiave: due compiti che contano la stessa parola non si perdono niente,
 * e compiti su parole diverse non si bloccano a vicenda.
 *
 * Seconda versione: HashMap e synchronized attorno al solo aggiornamento. Corretta, ma
 * ogni parola di ogni file passa dallo stesso monitor: con molti file e molti core la
 * contesa si sente. (Con tre file di tre righe i tempi sono tutti trascurabili: per la
 * misura servono decine di file grandi, per esempio una cartella di sorgenti.)
 *
 * Uso: java ... ParoleDiMoltiFile [cartella]   (default res/e06)
 *
 * Errore tipico: mettere la lettura del file dentro il synchronized della seconda
 * versione: il pool diventa un thread solo.
 */
public class ParoleDiMoltiFile {

    public static void main(String[] args) throws Exception {
        Path cartella = Path.of(args.length > 0 ? args[0] : "res/e06");
        List<Path> file;
        try (Stream<Path> s = Files.list(cartella)) {
            file = s.filter(p -> p.toString().endsWith(".txt")).toList();
        }

        long t0 = System.nanoTime();
        ConcurrentMap<String, Integer> conteggi = new ConcurrentHashMap<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (Path p : file)
                executor.execute(() -> {
                    for (String parola : parole(p))
                        conteggi.merge(parola, 1, Integer::sum);     // atomico per chiave
                });
        }
        System.out.printf("ConcurrentHashMap: %d parole distinte in %d ms%n", conteggi.size(), (System.nanoTime() - t0) / 1_000_000);
        conteggi.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.printf("  %-12s %d%n", e.getKey(), e.getValue()));

        t0 = System.nanoTime();
        Map<String, Integer> conteggi2 = new HashMap<>();
        Object lock = new Object();
        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (Path p : file)
                executor.execute(() -> {
                    List<String> parole = parole(p);                     // lettura fuori dal lock
                    synchronized (lock) {
                        for (String parola : parole) conteggi2.merge(parola, 1, Integer::sum);
                    }
                });
        }
        System.out.printf("HashMap + synchronized: %d parole distinte in %d ms%n", conteggi2.size(), (System.nanoTime() - t0) / 1_000_000);
    }

    static List<String> parole(Path p) {
        try {
            return Stream.of(Files.readString(p).toLowerCase().split("[^\\p{L}']+"))
                    .filter(w -> !w.isBlank())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
