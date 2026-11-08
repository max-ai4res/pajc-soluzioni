package it.unibs.pajc.redolfi.e04;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Il modello del flipper: un piano inclinato visto dall'alto (gravita ridotta verso il
 * basso), pareti a sinistra, destra e in alto, due respingenti circolari con restituzione
 * maggiore di uno, due alette che ruotano attorno a un perno, il foro in fondo.
 * Il motore e' quello del capitolo ridotto a una pallina; le novita stanno in respingente()
 * e aletta(): la velocita del punto di contatto di un'aletta che ruota entra nell'urto come
 * velocita relativa, con massa infinita (la pallina rimbalza, l'aletta no).
 */
public class Flipper {

    public static final double PASSO = 1.0 / 400;
    public static final double LARGHEZZA = 4, ALTEZZA = 7;
    private static final double G = 3.0, V_MAX = 14;

    public record Respingente(Vec2 centro, double raggio) {
    }

    /** Un'aletta: il perno, la lunghezza, l'angolo a riposo e quello alzato (gradi), il verso. */
    public class Aletta {
        final Vec2 perno;
        final double lunghezza, riposo, alzata;
        double angolo, omega;                            // gradi e gradi/s
        boolean premuta;

        Aletta(Vec2 perno, double lunghezza, double riposo, double alzata) {
            this.perno = perno;
            this.lunghezza = lunghezza;
            this.riposo = riposo;
            this.alzata = alzata;
            this.angolo = riposo;
        }

        public Vec2 perno() { return perno; }

        public Vec2 punta() {
            double a = Math.toRadians(angolo);
            return perno.piu(new Vec2(Math.cos(a), Math.sin(a)).per(lunghezza));
        }

        void stepNext(double dt) {
            double meta = premuta ? alzata : riposo;
            double velocita = 600;                       // gradi al secondo
            double nuovo = angolo + Math.signum(meta - angolo) * Math.min(Math.abs(meta - angolo), velocita * dt);
            omega = (nuovo - angolo) / dt;
            angolo = nuovo;
        }
    }

    private Pallina pallina;
    private final List<Respingente> respingenti = new ArrayList<>();
    private final List<Aletta> alette = new ArrayList<>();
    private double accumulatore;
    private int punteggio, palline = 3;
    private final List<IntConsumer> listenerPunteggio = new ArrayList<>();

    public Flipper() {
        respingenti.add(new Respingente(new Vec2(1.3, 4.6), 0.35));
        respingenti.add(new Respingente(new Vec2(2.7, 4.6), 0.35));
        alette.add(new Aletta(new Vec2(0.9, 1.0), 1.0, -25, 15));
        alette.add(new Aletta(new Vec2(3.1, 1.0), 1.0, 205, 165));
        nuovaPallina();
    }

    public Pallina pallina()                 { return pallina; }
    public List<Respingente> respingenti()   { return respingenti; }
    public List<Aletta> alette()             { return alette; }
    public int punteggio()                   { return punteggio; }
    public int pallineRimaste()              { return palline; }

    public void addPunteggioListener(IntConsumer l) { listenerPunteggio.add(l); }

    public void premi(int quale, boolean giu) {
        alette.get(quale).premuta = giu;
    }

    private void nuovaPallina() {
        pallina = new Pallina(new Vec2(3.7, 1.5), 0.12, 0.6);
        pallina.setVelocita(new Vec2(0, 9));
    }

    private void punti(int n) {
        punteggio += n;
        for (IntConsumer l : List.copyOf(listenerPunteggio))
            l.accept(punteggio);
    }

    public void stepNext(double dt) {
        accumulatore += Math.min(dt, 0.1);
        while (accumulatore >= PASSO - 1e-12) {
            stepNext();
            accumulatore -= PASSO;
        }
    }

    private void stepNext() {
        for (Aletta a : alette)
            a.stepNext(PASSO);
        if (palline <= 0)
            return;
        Pallina p = pallina;
        Vec2 v = p.velocita().piu(new Vec2(0, -G * PASSO));
        if (v.modulo() > V_MAX)
            v = v.versore().per(V_MAX);                  // il tetto: i respingenti regalano energia
        p.setVelocita(v);
        p.setPosizione(p.posizione().piu(v.per(PASSO)));

        superficie(p, new Vec2(1, 0), p.posizione().x() - p.raggio(), Vec2.ZERO, 0.6);
        superficie(p, new Vec2(-1, 0), LARGHEZZA - p.posizione().x() - p.raggio(), Vec2.ZERO, 0.6);
        superficie(p, new Vec2(0, -1), ALTEZZA - p.posizione().y() - p.raggio(), Vec2.ZERO, 0.6);
        for (Respingente r : respingenti) {
            Vec2 d = p.posizione().meno(r.centro());
            double gap = d.modulo() - r.raggio() - p.raggio();
            if (gap < 0 && superficie(p, d.versore(), gap, Vec2.ZERO, 1.3))
                punti(100);
        }
        for (Aletta a : alette) {
            Vec2 ab = a.punta().meno(a.perno());
            double t = Math.clamp(p.posizione().meno(a.perno()).scalare(ab) / ab.scalare(ab), 0, 1);
            Vec2 q = a.perno().piu(ab.per(t));
            Vec2 d = p.posizione().meno(q);
            double gap = d.modulo() - p.raggio();
            if (gap < 0) {
                // la velocita del punto q dell'aletta: omega x r, con omega in rad/s
                Vec2 r = q.meno(a.perno());
                double w = Math.toRadians(a.omega);
                Vec2 vq = new Vec2(-w * r.y(), w * r.x());
                superficie(p, d.versore(), gap, vq, 0.7);
            }
        }
        if (p.posizione().y() < -p.raggio()) {           // il foro in fondo
            palline--;
            if (palline > 0)
                nuovaPallina();
        }
    }

    /**
     * Contatto con una superficie di massa infinita che si muove con velocita vs: come il
     * contatto del capitolo, ma sulla velocita relativa. Restituisce vero se c'e stato urto.
     */
    private boolean superficie(Pallina p, Vec2 n, double gap, Vec2 vs, double e) {
        if (gap >= 0)
            return false;
        p.setPosizione(p.posizione().meno(n.per(gap)));
        Vec2 rel = p.velocita().meno(vs);
        double vn = rel.scalare(n);
        if (vn >= 0)
            return false;
        Vec2 tangente = rel.meno(n.per(vn));
        p.setVelocita(vs.piu(tangente.per(0.9)).meno(n.per(vn * e)));
        return true;
    }
}
