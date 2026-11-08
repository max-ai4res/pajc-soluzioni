package it.unibs.pajc.redolfi.e01;

/**
 * Esercizio 5.1, il semaforo a stati.
 *
 * Idea: ogni fase e' una classe (State) che sa quanto dura, quali luci accende e come
 * risponde alla chiamata; il semaforo tiene la fase corrente e un solo metodo cambia().
 * FaseBase e' lo scheletro comune (Template Method): conta il tempo e chiede alla
 * sottoclasse la fase successiva.
 *
 * Confronto con l'enum del capitolo 2: con tre fasi l'enum con lo switch e' piu' corto e
 * si legge in una schermata; con dieci fasi, ognuna con regole sue (la chiamata, i
 * lampeggi, le fasce orarie), ogni switch cresce di dieci rami e la versione a classi
 * tiene ogni regola nella sua fase. Per tre fasi l'enum; per dieci gli stati.
 *
 * Errore tipico: riusare lo stesso oggetto fase per tutti i cicli, con il tempo trascorso
 * che riparte da quello del ciclo precedente. Qui ogni transizione crea una fase nuova.
 */
public class Semaforo {

    public enum Luce { ROSSO, GIALLO, VERDE }

    private Fase fase = new VerdeAuto();

    public Fase fase() { return fase; }

    /** L'unico posto in cui si cambia fase. */
    private void cambia(Fase nuova) {
        fase = nuova;
    }

    public void stepNext(double dt) {
        Fase prossima = fase.stepNext(dt);
        if (prossima != fase)
            cambia(prossima);
    }

    public void chiamata() {
        fase.chiamata();
    }

    public static void main(String[] args) {           // la prova senza finestra
        Semaforo s = new Semaforo();
        Fase ultima = null;
        for (int decimo = 0; decimo <= 600; decimo++) {
            if (decimo == 80)
                s.chiamata();
            if (s.fase() != ultima) {
                System.out.printf("t = %4.1f s  %-10s auto %-6s pedoni %s%n", decimo / 10.0,
                        s.fase().getClass().getSimpleName(), s.fase().luceAuto(), s.fase().lucePedoni());
                ultima = s.fase();
            }
            s.stepNext(0.1);
        }
    }
}
