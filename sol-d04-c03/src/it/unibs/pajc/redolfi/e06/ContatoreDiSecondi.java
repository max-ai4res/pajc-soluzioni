package it.unibs.pajc.redolfi.e06;

/**
 * Esercizio 3.6: un contatore di secondi.
 *
 * Idea: il thread stampa 1, 2, 3... dormendo un secondo fra una stampa e l'altra; main
 * dopo 5,5 secondi chiama interrupt() e poi join(). L'interruzione arriva *durante* lo
 * sleep, che lancia InterruptedException: nel catch si esce dal ciclo con return, e il
 * thread termina subito, a meta' del secondo in corso. Il join di main ritorna quando il
 * thread e' davvero finito, e solo allora si stampa "fermato".
 *
 * Errore tipico: catch vuoto. L'eccezione ha gia' abbassato la bandiera, il while
 * riprende e il contatore non si ferma mai; main resta bloccato nel join per sempre.
 */
public class ContatoreDiSecondi {

    public static void main(String[] args) throws InterruptedException {
        Thread contatore = new Thread(() -> {
            int n = 0;
            while (true) {
                System.out.println(++n);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("(richiesta di stop ricevuta al secondo " + n + ")");
                    return;                     // esce subito: il thread termina
                }
            }
        }, "contatore");

        contatore.start();
        Thread.sleep(5500);
        contatore.interrupt();                  // alza la bandiera
        contatore.join();                       // aspetta che abbia davvero finito
        System.out.println("fermato");
    }
}
