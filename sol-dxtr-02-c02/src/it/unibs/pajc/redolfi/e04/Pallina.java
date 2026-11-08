package it.unibs.pajc.redolfi.e04;

/** Una pallina del mondo: posizione e velocita in metri e secondi, raggio in metri. */
public class Pallina {

    private Vec2 posizione;        // origine in basso a sinistra, y in su
    private Vec2 velocita = Vec2.ZERO;
    private final double raggio;

    public Pallina(Vec2 posizione, double raggio) {
        this.posizione = posizione;
        this.raggio = raggio;
    }

    public Vec2 posizione() {
        return posizione;
    }

    public Vec2 velocita() {
        return velocita;
    }

    public double raggio() {
        return raggio;
    }

    /** Un passo di dt secondi: prima la velocita, poi la posizione. */
    void stepNext(double dt, Vec2 g) {
        velocita = velocita.piu(g.per(dt));
        posizione = posizione.piu(velocita.per(dt));
    }

    /** Rimbalzo ingenuo: a terra, e velocita verticale invertita. */
    void rimbalzaSulPavimento(double restituzione) {
        posizione = new Vec2(posizione.x(), raggio);
        velocita = new Vec2(velocita.x(), -velocita.y() * restituzione);
    }
}
