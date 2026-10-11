package it.unibs.pajc.redolfi.e08;

/**
 * Esercizio 3.8: MyTask educato.
 *
 * Il MyTask della dispensa con una sola modifica: nel catch dell'InterruptedException,
 * prima di uscire, stampa chi e' e a che passo e' arrivato. L'interfaccia della classe
 * (costruttore, run) non cambia. La variabile i e' visibile nel catch perche' il try e'
 * dentro il for.
 *
 * Errore tipico: dichiarare il contatore dentro il try, o mettere il try fuori dal for:
 * nel primo caso i non e' visibile nel catch, nel secondo si perde il passo corrente.
 */
public class MyTask implements Runnable {
    private final String nome;
    private final int nStep, delay;

    public MyTask(String nome, int nStep, int delay) {
        this.nome = nome; this.nStep = nStep; this.delay = delay;
    }

    public void run() {
        for (int i = 0; i < nStep; i++) {
            System.out.printf("%s, passo %2d [%s]%n", nome, i, Thread.currentThread().getName());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.printf("%s: interrotto al passo %d%n", nome, i);
                return;
            }
        }
        System.out.printf("%s: finito%n", nome);
    }
}
