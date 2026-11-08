package it.unibs.pajc.redolfi.e02;

/** Un segmento fisso da a a b, di spessore trascurabile: ci si rotola sopra e ci si rimbalza. */
public record Rampa(Vec2 a, Vec2 b) {

    /** Il punto del segmento piu vicino a p: la proiezione, limitata agli estremi. */
    public Vec2 puntoPiuVicino(Vec2 p) {
        Vec2 ab = b.meno(a);
        double t = Math.clamp(p.meno(a).scalare(ab) / ab.scalare(ab), 0, 1);
        return a.piu(ab.per(t));
    }
}
