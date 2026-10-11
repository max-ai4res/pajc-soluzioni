package it.unibs.pajc.redolfi.e10;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 5.10: otto scrittori che aggiungono eventi per due secondi, un lettore che
 * ogni 200 ms chiede un'istantanea e ne controlla la coerenza (gli istanti sono
 * crescenti? la dimensione non diminuisce mai?). Alla fine, quanti eventi e quante
 * istantanee.
 */
public class RegistroApp {

    public static void main(String[] args) throws InterruptedException {
        Registro registro = new Registro();
        long fine = System.nanoTime() + 2_000_000_000L;

        try (ExecutorService scrittori = Executors.newFixedThreadPool(8)) {
            for (int s = 0; s < 8; s++)
                scrittori.execute(() -> {
                    long n = 0;
                    while (System.nanoTime() < fine) registro.aggiungi("evento " + n++);
                });

            int istantanee = 0, precedente = 0;
            while (System.nanoTime() < fine) {
                Thread.sleep(200);
                List<Registro.Evento> foto = registro.istantanea();
                boolean monotona = foto.size() >= precedente;
                precedente = foto.size();
                istantanee++;
                System.out.printf("istantanea %d: %d eventi, dimensione non decrescente: %b%n",
                        istantanee, foto.size(), monotona);
            }
        }
        System.out.println("totale eventi: " + registro.istantanea().size());
    }
}
