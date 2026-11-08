package it.unibs.pajc.redolfi.e01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il mondo di Palline: una scatola larga e alta in metri, con il pavimento in basso, in
 * cui le palline cadono. Non sa niente di finestre e di pixel.
 */
public class Mondo {

    public static final Vec2 G = new Vec2(0, -9.81);     // m/s^2

    private final double larghezza, altezza;             // metri
    private final List<Pallina> palline = new ArrayList<>();

    public Mondo(double larghezza, double altezza) {
        this.larghezza = larghezza;
        this.altezza = altezza;
    }

    public double larghezza() {
        return larghezza;
    }

    public double altezza() {
        return altezza;
    }

    public List<Pallina> palline() {
        return Collections.unmodifiableList(palline);
    }

    /** Lascia una pallina ferma in un punto: da qui in poi cade da sola. */
    public void lascia(Vec2 punto, double raggio) {
        palline.add(new Pallina(punto, raggio));
    }

    /** Fa passare dt secondi: le palline cadono e rimbalzano. */
    public void stepNext(double dt) {
        stepNext(dt, 9.81);
    }

    /** Con una gravita data: cosi la configurazione puo passarla. */
    public void stepNext(double dt, double g) {
        for (Pallina p : palline) {
            p.stepNext(dt, new Vec2(0, -g));
            if (p.posizione().y() < p.raggio())
                p.rimbalzaSulPavimento(0.8);
        }
    }
}
