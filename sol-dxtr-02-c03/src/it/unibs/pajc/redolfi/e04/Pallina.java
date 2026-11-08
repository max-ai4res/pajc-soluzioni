package it.unibs.pajc.redolfi.e04;

/** Una pallina del mondo: posizione e velocita in metri e secondi, raggio in metri. */
public class Pallina {

    private Vec2 posizione;
    private Vec2 velocita = Vec2.ZERO;
    private final double raggio;
    private boolean ferma;

    public Pallina(Vec2 posizione, double raggio) {
        this.posizione = posizione;
        this.raggio = raggio;
    }

    public Vec2 posizione() { return posizione; }
    public Vec2 velocita()  { return velocita; }
    public double raggio()  { return raggio; }
    public boolean ferma()  { return ferma; }

    void stepNext(double dt, Vec2 g) {
        velocita = velocita.piu(g.per(dt));
        posizione = posizione.piu(velocita.per(dt));
    }

    void rimbalzaSulPavimento(double restituzione) {
        posizione = new Vec2(posizione.x(), raggio);
        velocita = new Vec2(velocita.x(), -velocita.y() * restituzione);
    }

    /** Si appoggia al pavimento e smette di muoversi. */
    void fermati() {
        posizione = new Vec2(posizione.x(), raggio);
        velocita = Vec2.ZERO;
        ferma = true;
    }
}
