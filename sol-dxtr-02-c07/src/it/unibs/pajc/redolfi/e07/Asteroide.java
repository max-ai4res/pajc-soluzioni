package it.unibs.pajc.redolfi.e07;

public class Asteroide extends Entita {

    public enum Taglia { GRANDE(1.4, 20), MEDIO(0.8, 50), PICCOLO(0.4, 100);
        public final double raggio; public final int punti;
        Taglia(double r, int p) { raggio = r; punti = p; }
    }

    private final Taglia taglia;
    private final double rotazione;
    private boolean colpito;

    public Asteroide(Vec2 posizione, Vec2 velocita, Taglia taglia, double rotazione) {
        super(posizione);
        this.velocita = velocita;
        this.taglia = taglia;
        this.rotazione = rotazione;
    }

    public Taglia taglia() { return taglia; }

    @Override
    public double raggio() { return taglia.raggio; }

    @Override
    protected void dopoIlMoto(double dt) {
        angolo += rotazione * dt;
    }

    @Override
    public boolean viva() { return !colpito; }

    void colpisci() { colpito = true; }
}
