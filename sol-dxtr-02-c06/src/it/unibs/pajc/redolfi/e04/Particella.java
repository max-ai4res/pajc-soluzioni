package it.unibs.pajc.redolfi.e04;

/** Una scheggia dello schizzo: vive mezzo secondo e poi torna nel pool, pronta a rinascere. */
public class Particella extends Entita {

    private static final double VITA = 0.5;

    Particella() {
        super(Vec2.ZERO);
        rinasci(Vec2.ZERO, Vec2.ZERO);
        spegni();
    }

    @Override
    protected double pesoDellaGravita() { return 0.4; }

    @Override
    protected void dopoIlMoto(double dt) {
        velocita = velocita.per(1 - 2 * dt);
    }

    @Override
    public boolean viva() { return eta() < VITA; }

    public double vitaResidua() { return 1 - eta() / VITA; }

    /** Il pool la riusa: stesso oggetto, stato azzerato. Tutto, non solo la posizione. */
    void accendi(Vec2 posizione, Vec2 velocita) {
        rinasci(posizione, velocita);
    }

    void spegni() {
        rinasci(posizione, Vec2.ZERO);
        // un'eta oltre la vita: non viva, disponibile per il pool
        avanzaFinoAllaFine();
    }

    private void avanzaFinoAllaFine() {
        while (viva())
            stepNext(VITA, Vec2.ZERO);
    }
}
