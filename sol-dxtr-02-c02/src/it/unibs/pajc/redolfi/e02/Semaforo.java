package it.unibs.pajc.redolfi.e02;

/**
 * Il modello: un ciclo di tre fasi, il tempo che scorre solo attraverso stepNext(dt), la
 * chiamata pedonale che accorcia il verde delle auto. Niente Swing, niente timer.
 */
public class Semaforo {

    public enum Fase {
        VERDE_AUTO(20), GIALLO(3), ROSSO_AUTO(12);

        public final double durata;          // secondi

        Fase(double durata) {
            this.durata = durata;
        }

        Fase prossima() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Luce { ROSSO, GIALLO, VERDE }

    private Fase fase = Fase.VERDE_AUTO;
    private double tempoNellaFase;           // secondi dall'inizio della fase
    private double durataCorrente = fase.durata;

    public Fase fase() {
        return fase;
    }

    public double tempoNellaFase() {
        return tempoNellaFase;
    }

    public Luce luceAuto() {
        return switch (fase) {
            case VERDE_AUTO -> Luce.VERDE;
            case GIALLO -> Luce.GIALLO;
            case ROSSO_AUTO -> Luce.ROSSO;
        };
    }

    public Luce lucePedoni() {
        return fase == Fase.ROSSO_AUTO ? Luce.VERDE : Luce.ROSSO;
    }

    /** Fa passare dt secondi, cambiando fase tutte le volte che serve (anche piu' di una). */
    public void stepNext(double dt) {
        tempoNellaFase += dt;
        while (tempoNellaFase >= durataCorrente) {
            tempoNellaFase -= durataCorrente;
            fase = fase.prossima();
            durataCorrente = fase.durata;
        }
    }

    /** La chiamata pedonale: se le auto hanno il verde da almeno 5 s, il verde finisce fra 2 s. */
    public void chiamata() {
        if (fase == Fase.VERDE_AUTO && tempoNellaFase >= 5)
            durataCorrente = Math.min(durataCorrente, tempoNellaFase + 2);
    }
}
