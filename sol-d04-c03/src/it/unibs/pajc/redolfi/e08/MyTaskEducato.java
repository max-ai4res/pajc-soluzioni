package it.unibs.pajc.redolfi.e08;

/**
 * Esercizio 3.8: tre MyTask con passi e ritardi diversi, interrotti tutti da main dopo
 * un tempo fissato. I thread stanno in un array: un ciclo per gli start, uno per gli
 * interrupt, uno per i join (cosi' main sa quando sono davvero tutti finiti). Il
 * compito C, con 3 passi da 100 ms, finisce da solo prima dell'interruzione: interrupt()
 * su un thread terminato non fa niente, ed e' corretto cosi'.
 */
public class MyTaskEducato {

    public static void main(String[] args) throws InterruptedException {
        Thread[] thread = {
            new Thread(new MyTask("A", 20, 150)),
            new Thread(new MyTask("B", 20, 230)),
            new Thread(new MyTask("C", 3, 100)),
        };
        for (Thread t : thread) t.start();

        Thread.sleep(1000);
        System.out.println("main: basta cosi'");
        for (Thread t : thread) t.interrupt();
        for (Thread t : thread) t.join();
        System.out.println("main: tutti fermi");
    }
}
