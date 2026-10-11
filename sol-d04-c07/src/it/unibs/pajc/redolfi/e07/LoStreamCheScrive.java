package it.unibs.pajc.redolfi.e07;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

/**
 * Esercizio 7.7: lo stream che scrive.
 *
 * 1. ArrayList riempita da forEach parallelo: le add da thread diversi si
 *    sovrappongono su array e size; il risultato e' una dimensione sbagliata o una
 *    ArrayIndexOutOfBoundsException (dipende dall'intreccio: il programma lo ripete
 *    finche' non mostra almeno un esito sbagliato).
 * 2. Collections.synchronizedList: corretto, ma ogni add prende un lock: il
 *    parallelismo si paga invece di guadagnarlo.
 * 3. ConcurrentLinkedQueue: corretto, senza lock, ordine non garantito.
 * 4. boxed().toList(): il modo giusto. Ogni thread del pool riempie una lista *propria*
 *    (confinamento), e la libreria concatena le liste alla fine, in ordine: nessun dato
 *    condiviso durante il lavoro, quindi nessun lock.
 */
public class LoStreamCheScrive {

    static final int N = 100_000;

    public static void main(String[] args) {
        // 1. la lista che perde
        for (int tentativo = 1; tentativo <= 20; tentativo++) {
            List<Integer> l = new ArrayList<>();
            try {
                IntStream.range(0, N).parallel().forEach(l::add);
                if (l.size() != N) {
                    System.out.println("ArrayList: size = " + l.size() + " invece di " + N + " (tentativo " + tentativo + ")");
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("ArrayList: " + e + " (tentativo " + tentativo + ")");
                break;
            }
            if (tentativo == 20) System.out.println("ArrayList: stavolta e' andata bene 20 volte: e' il modo peggiore di essere sbagliato");
        }

        // 2. lista sincronizzata
        long t0 = System.nanoTime();
        List<Integer> sync = Collections.synchronizedList(new ArrayList<>());
        IntStream.range(0, N).parallel().forEach(sync::add);
        System.out.printf("synchronizedList: size = %d in %d ms%n", sync.size(), (System.nanoTime() - t0) / 1_000_000);

        // 3. coda concorrente
        t0 = System.nanoTime();
        ConcurrentLinkedQueue<Integer> coda = new ConcurrentLinkedQueue<>();
        IntStream.range(0, N).parallel().forEach(coda::add);
        System.out.printf("ConcurrentLinkedQueue: size = %d in %d ms (primo elemento: %d)%n", coda.size(), (System.nanoTime() - t0) / 1_000_000, coda.peek());

        // 4. il collettore
        t0 = System.nanoTime();
        List<Integer> lista = IntStream.range(0, N).parallel().boxed().toList();
        System.out.printf("toList(): size = %d in %d ms, in ordine: %b%n", lista.size(), (System.nanoTime() - t0) / 1_000_000,
                lista.get(0) == 0 && lista.get(N - 1) == N - 1);
    }
}
