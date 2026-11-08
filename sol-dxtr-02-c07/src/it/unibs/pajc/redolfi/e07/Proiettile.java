package it.unibs.pajc.redolfi.e07;

/** Vive due secondi; viene da un pool: spara() lo riaccende azzerando tutto. */
public class Proiettile extends Entita {

    static final double VITA = 2.0;
    private boolean spento = true;

    Proiettile() {
        super(Vec2.ZERO);
    }

    @Override
    public double raggio() { return 0.08; }

    @Override
    public boolean viva() { return !spento && eta() < VITA; }

    void spara(Vec2 da, Vec2 v) {
        setPosizione(da);
        setVelocita(v);
        azzeraEta();
        spento = false;
    }

    void spegni() { spento = true; }
}
