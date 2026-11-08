package it.unibs.pajc.redolfi.e04;

/** Lo scheletro di tutto cio' che si muove (Template Method, capitolo 5). */
public abstract class Entita {

    protected Vec2 posizione;
    protected Vec2 velocita = Vec2.ZERO;
    private double eta;

    protected Entita(Vec2 posizione) {
        this.posizione = posizione;
    }

    public Vec2 posizione() { return posizione; }
    public Vec2 velocita()  { return velocita; }
    public double eta()     { return eta; }

    public final void stepNext(double dt, Vec2 g) {
        velocita = velocita.piu(g.per(dt * pesoDellaGravita()));
        posizione = posizione.piu(velocita.per(dt));
        eta += dt;
        dopoIlMoto(dt);
    }

    protected double pesoDellaGravita() { return 1; }

    protected void dopoIlMoto(double dt) { }

    public boolean viva() { return true; }

    /** Riparte da zero in un altro punto: serve a chi riusa le entita invece di crearle. */
    protected void rinasci(Vec2 posizione, Vec2 velocita) {
        this.posizione = posizione;
        this.velocita = velocita;
        this.eta = 0;
    }

    void setPosizione(Vec2 p) { posizione = p; }
    void setVelocita(Vec2 v)  { velocita = v; }
}
