package it.unibs.pajc.redolfi.e07;

/** Un vettore del piano. Immutabile. */
public record Vec2(double x, double y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 piu(Vec2 v)      { return new Vec2(x + v.x, y + v.y); }
    public Vec2 meno(Vec2 v)     { return new Vec2(x - v.x, y - v.y); }
    public Vec2 per(double k)    { return new Vec2(x * k, y * k); }
    public double modulo()       { return Math.hypot(x, y); }

    /** Prodotto scalare: quanto di questo vettore va nella direzione di v. */
    public double scalare(Vec2 v) { return x * v.x + y * v.y; }

    /** Lo stesso verso, lunghezza uno (il vettore nullo resta nullo). */
    public Vec2 versore() {
        double m = modulo();
        return m == 0 ? ZERO : new Vec2(x / m, y / m);
    }
}
