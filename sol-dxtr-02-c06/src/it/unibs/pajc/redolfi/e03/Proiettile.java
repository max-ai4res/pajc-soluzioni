package it.unibs.pajc.redolfi.e03;

/** Un proiettile: vive due secondi, attraversa i bordi. Lo stato si azzera in un posto solo. */
public class Proiettile {

    static final double VITA = 2.0;

    private Vec2 posizione = Vec2.ZERO, velocita = Vec2.ZERO;
    private double eta = VITA;                           // nasce spento

    public Vec2 posizione() { return posizione; }
    public boolean vivo()   { return eta < VITA; }

    /** L'unico metodo che inizializza: chiamato dal pool, azzera tutto. */
    void spara(Vec2 da, Vec2 v) {
        posizione = da;
        velocita = v;
        eta = 0;
    }

    void stepNext(double dt, double larghezza, double altezza) {
        if (!vivo())
            return;
        posizione = posizione.piu(velocita.per(dt));
        double x = Math.floorMod((long) Math.floor(posizione.x() * 1000), (long) (larghezza * 1000)) / 1000.0;
        double y = Math.floorMod((long) Math.floor(posizione.y() * 1000), (long) (altezza * 1000)) / 1000.0;
        posizione = new Vec2(x, y);                      // il toro
        eta += dt;
    }
}
