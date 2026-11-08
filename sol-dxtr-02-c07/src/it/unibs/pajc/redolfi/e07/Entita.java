package it.unibs.pajc.redolfi.e07;

/** Lo scheletro di tutto cio' che vola nell'universo toroidale (Template Method). */
public abstract class Entita {

    protected Vec2 posizione, velocita = Vec2.ZERO;
    protected double angolo;                             // radianti
    private double eta;

    protected Entita(Vec2 posizione) {
        this.posizione = posizione;
    }

    public Vec2 posizione() { return posizione; }
    public Vec2 velocita()  { return velocita; }
    public double angolo()  { return angolo; }
    public double eta()     { return eta; }

    /** Il raggio del cerchio con cui il modello decide le collisioni. */
    public abstract double raggio();

    public final void stepNext(double dt, double larghezza, double altezza) {
        primaDelMoto(dt);
        posizione = posizione.piu(velocita.per(dt));
        posizione = new Vec2(toro(posizione.x(), larghezza), toro(posizione.y(), altezza));
        eta += dt;
        dopoIlMoto(dt);
    }

    private static double toro(double v, double max) {
        return ((v % max) + max) % max;                  // il resto positivo
    }

    protected void primaDelMoto(double dt) { }

    protected void dopoIlMoto(double dt) { }

    public boolean viva() { return true; }

    void setPosizione(Vec2 p) { posizione = p; }
    void setVelocita(Vec2 v)  { velocita = v; }
    void azzeraEta()          { eta = 0; }

    public boolean tocca(Entita altra) {
        return posizione.meno(altra.posizione).modulo() < raggio() + altra.raggio();
    }
}
