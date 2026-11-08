package it.unibs.pajc.redolfi.e01;

/** Una fase del semaforo: sa quanto dura, che luci accende, e che cosa fa la chiamata pedonale. */
public interface Fase {

    Semaforo.Luce luceAuto();

    Semaforo.Luce lucePedoni();

    /** dt secondi in questa fase; restituisce la fase successiva quando e' ora di cambiare, altrimenti this. */
    Fase stepNext(double dt);

    /** La chiamata pedonale: di norma non fa niente. */
    default void chiamata() {
    }

    double tempoTrascorso();
}
