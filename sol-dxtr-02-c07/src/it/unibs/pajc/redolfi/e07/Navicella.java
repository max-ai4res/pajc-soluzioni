package it.unibs.pajc.redolfi.e07;

/** La navicella: inerzia, attrito, e l'invulnerabilita' come stato dell'entita' con un tempo. */
public class Navicella extends Entita {

    private double giro, spinta;                         // -1..1, 0..1 dal controllore
    private double invulnerabile;                        // secondi che restano

    public Navicella(Vec2 posizione) {
        super(posizione);
        angolo = Math.PI / 2;
    }

    @Override
    public double raggio() { return 0.5; }

    public boolean invulnerabile() { return invulnerabile > 0; }

    public void comanda(double giro, double spinta) {
        this.giro = giro;
        this.spinta = spinta;
    }

    @Override
    protected void primaDelMoto(double dt) {
        angolo += giro * 3.5 * dt;
        Vec2 dir = new Vec2(Math.cos(angolo), Math.sin(angolo));
        velocita = velocita.piu(dir.per(spinta * 12 * dt)).per(1 - 0.4 * dt);   // spinta e attrito
    }

    @Override
    protected void dopoIlMoto(double dt) {
        invulnerabile = Math.max(0, invulnerabile - dt);  // si spegne da sola
    }

    void rinasci(Vec2 dove) {
        setPosizione(dove);
        setVelocita(Vec2.ZERO);
        angolo = Math.PI / 2;
        invulnerabile = 3;
    }
}
