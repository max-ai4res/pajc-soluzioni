package it.unibs.pajc.redolfi.e03;

/** Una pallina: posizione e velocita (m, m/s), raggio, massa proporzionale all'area, restituzione. */
public class Pallina {

    private Vec2 posizione;
    private Vec2 velocita = Vec2.ZERO;
    private final double raggio, massa, restituzione;
    private boolean ferma;

    public Pallina(Vec2 posizione, double raggio, double restituzione) {
        this.posizione = posizione;
        this.raggio = raggio;
        this.massa = Math.PI * raggio * raggio;          // densita uno: conta solo il rapporto
        this.restituzione = restituzione;
    }

    public Vec2 posizione()       { return posizione; }
    public Vec2 velocita()        { return velocita; }
    public double raggio()        { return raggio; }
    public double massa()         { return massa; }
    public double restituzione()  { return restituzione; }
    public boolean ferma()        { return ferma; }

    void setPosizione(Vec2 p) { posizione = p; }

    void setVelocita(Vec2 v) {
        velocita = v;
        ferma = false;                                   // chi riceve una spinta si sveglia
    }

    void fermati() {
        velocita = Vec2.ZERO;
        ferma = true;
    }

    /** Energia cinetica piu potenziale rispetto a y = 0, con la gravita g (positiva). */
    public double energia(double g) {
        return 0.5 * massa * velocita.scalare(velocita) + massa * g * posizione.y();
    }
}
