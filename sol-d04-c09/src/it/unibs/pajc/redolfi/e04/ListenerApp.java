package it.unibs.pajc.redolfi.e04;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Palestra 9.4: quattro notificatori, un thread che aggiunge e toglie, un listener che si rimuove da solo. */
public class ListenerApp {

    public static void main(String[] args) throws InterruptedException {
        for (boolean cow : new boolean[] { false, true }) {
            System.out.println("== " + (cow ? "CopyOnWriteArrayList" : "ArrayList"));
            Sorgente sorgente = new Sorgente(cow);
            AtomicInteger ricevuti = new AtomicInteger();
            Consumer<String>[] autoRimozione = new Consumer[1];
            autoRimozione[0] = new Consumer<>() {
                int visti = 0;
                public void accept(String e) {
                    if (++visti == 3) {
                        System.out.println("  il listener si rimuove da solo durante la notifica di " + e);
                        sorgente.removeListener(this);          // modifica durante l'iterazione
                    }
                }
            };
            sorgente.addListener(autoRimozione[0]);
            sorgente.addListener(e -> ricevuti.incrementAndGet());

            try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
                for (int t = 0; t < 4; t++) {
                    int id = t;
                    executor.execute(() -> {
                        try {
                            for (int i = 0; i < 1000; i++) sorgente.notifica("evento " + id + "/" + i);
                        } catch (RuntimeException ex) {
                            System.out.println("  " + Thread.currentThread().getName() + ": " + ex);
                        }
                    });
                }
                executor.execute(() -> {
                    for (int i = 0; i < 200; i++) {
                        Consumer<String> l = e -> { };
                        sorgente.addListener(l);
                        sorgente.removeListener(l);
                    }
                });
            }
            System.out.println("  eventi ricevuti dal contatore: " + ricevuti.get() + " (attesi 4000)");
        }
    }
}
