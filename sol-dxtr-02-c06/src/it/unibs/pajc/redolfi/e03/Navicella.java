package it.unibs.pajc.redolfi.e03;

/** Il modello: la navicella al centro che ruota e spara, i proiettili dal pool, l'universo toroidale. */
public class Navicella {

    public static final double LARGHEZZA = 16, ALTEZZA = 12;

    private double angolo;                               // radianti, 0 = verso destra
    private final PoolProiettili pool = new PoolProiettili(64);
    private double giro;                                 // -1, 0, +1

    public double angolo()         { return angolo; }
    public Vec2 posizione()        { return new Vec2(LARGHEZZA / 2, ALTEZZA / 2); }
    public PoolProiettili pool()   { return pool; }

    public void ruota(double verso) { giro = verso; }

    public boolean spara() {
        Vec2 dir = new Vec2(Math.cos(angolo), Math.sin(angolo));
        return pool.spara(posizione().piu(dir.per(0.6)), dir.per(9));
    }

    public void stepNext(double dt) {
        angolo += giro * 3 * dt;
        for (Proiettile p : pool.tutti())
            p.stepNext(dt, LARGHEZZA, ALTEZZA);
    }
}
