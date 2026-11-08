package it.unibs.pajc.redolfi.e01;

/** Un vettore del piano: posizione, spostamento, velocita, accelerazione. Immutabile. */
public record Vec2(double x, double y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 piu(Vec2 v) {
        return new Vec2(x + v.x, y + v.y);
    }

    public Vec2 meno(Vec2 v) {
        return new Vec2(x - v.x, y - v.y);
    }

    public Vec2 per(double k) {
        return new Vec2(x * k, y * k);
    }

    public double modulo() {
        return Math.hypot(x, y);
    }
}
