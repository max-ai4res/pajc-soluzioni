package it.unibs.pajc.redolfi.e05;

import java.util.Comparator;
import java.util.List;

/**
 * Esercizio 2.5: i thread di una JVM vuota.
 *
 * Idea: Thread.getAllStackTraces() restituisce una mappa thread -> stack di tutti i
 * thread vivi che sono oggetti Thread; ne stampiamo nome, stato e flag daemon, ordinati
 * per nome. Il main non crea nessun thread, eppure ne trova cinque o sei: sono della JVM
 * (gestione dei riferimenti, finalizzazione, segnali) e sono tutti daemon, per questo il
 * programma termina appena main finisce. `jstack` ne mostra una ventina, perche' vede
 * anche i thread nativi (garbage collector, compilatore JIT) che non hanno un oggetto
 * Thread. Il numero cambia da una JVM all'altra: alcuni thread nascono su richiesta.
 *
 * Errore tipico: cercare questi thread nel proprio codice, o credere che un thread
 * daemon "faccia qualcosa di nascosto" per il programma: non tiene vivo nulla e viene
 * terminato senza preavviso quando l'ultimo thread non daemon finisce.
 */
public class ThreadDiUnaJvmVuota {

    public static void main(String[] args) {
        List<Thread> thread = Thread.getAllStackTraces().keySet().stream()
                .sorted(Comparator.comparing(Thread::getName))
                .toList();

        System.out.printf("%-28s %-14s %s%n", "nome", "stato", "daemon");
        for (Thread t : thread) {
            System.out.printf("%-28s %-14s %s%n", t.getName(), t.getState(), t.isDaemon());
        }
        System.out.println("totale: " + thread.size() + " thread");
    }
}
