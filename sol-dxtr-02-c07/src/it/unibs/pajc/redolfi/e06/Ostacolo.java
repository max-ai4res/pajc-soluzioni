package it.unibs.pajc.redolfi.e06;

/** Un rettangolo fisso del mondo, con l'angolo in basso a sinistra in (x, y). */
public record Ostacolo(double x, double y, double larghezza, double altezza) {

    /** Il punto del rettangolo piu vicino a p (p stesso se e' dentro). */
    public Vec2 puntoPiuVicino(Vec2 p) {
        return new Vec2(Math.clamp(p.x(), x, x + larghezza),
                        Math.clamp(p.y(), y, y + altezza));
    }
}
