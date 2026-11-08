package it.unibs.pajc.redolfi.e03;

import java.util.ArrayList;
import java.util.List;

/**
 * Il modello di Pong: campo senza gravita', due racchette (ostacoli che si muovono), una
 * pallina a passo fisso. Le racchette ricevono una velocita' dal controllore a ogni passo
 * (comando continuo); nell'urto la velocita' della racchetta entra come velocita' relativa.
 */
public class Pong {

    public interface Listener {
        default void punto(int a, int b) { }
        default void rimbalzo() { }
    }

    public static final double LARGHEZZA = 16, ALTEZZA = 10, PASSO = 1.0 / 240;
    static final double RACCHETTA_H = 2.0, RACCHETTA_W = 0.3, RAGGIO = 0.2, V_RACCHETTA = 8;

    private double yA = ALTEZZA / 2, yB = ALTEZZA / 2;            // centri delle racchette
    private double vA, vB;                                         // -1, 0, +1 dal controllore
    private Vec2 pos = new Vec2(LARGHEZZA / 2, ALTEZZA / 2), vel = new Vec2(6, 2.5);
    private int puntiA, puntiB;
    private double accumulatore;
    private final List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener l) { listeners.add(l); }

    public double yA()      { return yA; }
    public double yB()      { return yB; }
    public Vec2 pallina()   { return pos; }
    public Vec2 velocita()  { return vel; }
    public int puntiA()     { return puntiA; }
    public int puntiB()     { return puntiB; }

    public void muovi(double versoA, double versoB) {
        vA = versoA;
        vB = versoB;
    }

    public void lancia(Vec2 v) {
        pos = new Vec2(LARGHEZZA / 2, ALTEZZA / 2);
        vel = v;
    }

    public void stepNext(double dt) {
        accumulatore += Math.min(dt, 0.1);
        while (accumulatore >= PASSO - 1e-12) {
            stepNext();
            accumulatore -= PASSO;
        }
    }

    private void stepNext() {
        yA = Math.clamp(yA + vA * V_RACCHETTA * PASSO, RACCHETTA_H / 2, ALTEZZA - RACCHETTA_H / 2);
        yB = Math.clamp(yB + vB * V_RACCHETTA * PASSO, RACCHETTA_H / 2, ALTEZZA - RACCHETTA_H / 2);
        pos = pos.piu(vel.per(PASSO));
        if (pos.y() - RAGGIO < 0 && vel.y() < 0 || pos.y() + RAGGIO > ALTEZZA && vel.y() > 0) {
            vel = new Vec2(vel.x(), -vel.y());
            annunciaRimbalzo();
        }
        racchetta(1.0, yA, vA * V_RACCHETTA, true);
        racchetta(LARGHEZZA - 1.0, yB, vB * V_RACCHETTA, false);
        if (pos.x() < -RAGGIO) {
            puntiB++;
            lancia(new Vec2(6, 2.5));
            for (Listener l : List.copyOf(listeners))
                l.punto(puntiA, puntiB);
        } else if (pos.x() > LARGHEZZA + RAGGIO) {
            puntiA++;
            lancia(new Vec2(-6, -2.5));
            for (Listener l : List.copyOf(listeners))
                l.punto(puntiA, puntiB);
        }
    }

    /** L'urto con una racchetta: il punto piu vicino del rettangolo, la normale, la velocita relativa. */
    private void racchetta(double x, double y, double vy, boolean sinistra) {
        double qx = Math.clamp(pos.x(), x - RACCHETTA_W / 2, x + RACCHETTA_W / 2);
        double qy = Math.clamp(pos.y(), y - RACCHETTA_H / 2, y + RACCHETTA_H / 2);
        Vec2 d = pos.meno(new Vec2(qx, qy));
        double dist = d.modulo();
        if (dist >= RAGGIO)
            return;
        Vec2 n = dist == 0 ? new Vec2(sinistra ? 1 : -1, 0) : d.versore();
        pos = pos.piu(n.per(RAGGIO - dist));
        Vec2 rel = vel.meno(new Vec2(0, vy));
        double vn = rel.scalare(n);
        if (vn >= 0)
            return;
        Vec2 nuova = rel.meno(n.per(2 * vn)).piu(new Vec2(0, vy * 0.5));   // la racchetta "taglia"
        vel = nuova.versore().per(Math.min(14, nuova.modulo() * 1.03));      // un po' piu veloce a ogni colpo
        annunciaRimbalzo();
    }

    private void annunciaRimbalzo() {
        for (Listener l : List.copyOf(listeners))
            l.rimbalzo();
    }
}
