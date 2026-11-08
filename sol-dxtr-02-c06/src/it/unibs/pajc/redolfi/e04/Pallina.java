package it.unibs.pajc.redolfi.e04;

/** Una pallina: un'entita con un raggio, un materiale e la quiete. */
public class Pallina extends Entita {

    private final double raggio;
    private final Materiale materiale;
    private boolean ferma, scoppiata;

    public Pallina(Vec2 posizione, double raggio, Materiale materiale) {
        super(posizione);
        this.raggio = raggio;
        this.materiale = materiale;
    }

    public double raggio()        { return raggio; }
    public Materiale materiale()  { return materiale; }
    public double massa() {
        return materiale.densita() * Math.PI * raggio * raggio;
    }
    public double restituzione()  { return materiale.restituzione(); }
    public boolean ferma()        { return ferma; }

    @Override
    public boolean viva() {
        return !scoppiata;
    }

    @Override
    void setVelocita(Vec2 v) {
        super.setVelocita(v);
        ferma = false;                                   // chi riceve una spinta si sveglia
    }

    void fermati() {
        super.setVelocita(Vec2.ZERO);
        ferma = true;
    }

    void scoppia() {
        scoppiata = true;
    }

    public double energia(double g) {
        return 0.5 * massa() * velocita.scalare(velocita) + massa() * g * posizione.y();
    }
}
