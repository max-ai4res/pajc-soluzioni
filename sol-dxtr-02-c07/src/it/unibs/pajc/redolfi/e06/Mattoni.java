package it.unibs.pajc.redolfi.e06;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il modello di Mattoni: racchetta (comando continuo), pallina a passo fisso con
 * sottopassi, mattoni come ostacoli con colpi residui tolti a fine passo, punteggio e
 * vite come proprieta', eventi per i suoni.
 */
public class Mattoni {

    public interface Listener {
        default void mattoneRotto(Mattone m, int punti) { }
        default void rimbalzo() { }
        default void pallinaPersa() { }
        default void livelloFinito() { }
    }

    public static final double LARGHEZZA = 12, ALTEZZA = 9, PASSO = 1.0 / 240;
    static final double RACCHETTA_W = 2.0, RACCHETTA_H = 0.3, RAGGIO = 0.15, V_RACCHETTA = 9, V_PALLINA = 8;

    private double racchettaX = LARGHEZZA / 2, versoRacchetta;
    private Vec2 pos = new Vec2(LARGHEZZA / 2, RACCHETTA_H + RAGGIO), vel = Vec2.ZERO;
    private boolean incollata = true;
    private final List<Mattone> mattoni = new ArrayList<>();
    private int punteggio, vite = 3;
    private double accumulatore;
    private final List<Listener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);
    private final List<Mattone> rottiNelPasso = new ArrayList<>();

    public void addListener(Listener l) { listeners.add(l); }
    public void addPropertyChangeListener(PropertyChangeListener l) { proprieta.addPropertyChangeListener(l); }

    public double racchettaX()       { return racchettaX; }
    public Vec2 pallina()            { return pos; }
    public boolean incollata()       { return incollata; }
    public List<Mattone> mattoni()   { return Collections.unmodifiableList(mattoni); }
    public int punteggio()           { return punteggio; }
    public int vite()                { return vite; }

    public void caricaLivello(List<Mattone> livello) {
        mattoni.clear();
        mattoni.addAll(livello);
        incolla();
    }

    public void muovi(double verso) { versoRacchetta = verso; }

    /** La barra spaziatrice: la pallina parte dalla racchetta. */
    public void lancia() {
        if (incollata) {
            incollata = false;
            vel = new Vec2(V_PALLINA * 0.4, V_PALLINA * 0.9);
        }
    }

    private void incolla() {
        incollata = true;
        vel = Vec2.ZERO;
        pos = new Vec2(racchettaX, RACCHETTA_H + RAGGIO);
    }

    private void setPunteggio(int p) {
        int vecchio = punteggio;
        punteggio = p;
        proprieta.firePropertyChange("punteggio", vecchio, p);
    }

    private void setVite(int v) {
        int vecchie = vite;
        vite = v;
        proprieta.firePropertyChange("vite", vecchie, v);
    }

    public void stepNext(double dt) {
        accumulatore += Math.min(dt, 0.1);
        while (accumulatore >= PASSO - 1e-12) {
            stepNext();
            accumulatore -= PASSO;
        }
    }

    private void stepNext() {
        racchettaX = Math.clamp(racchettaX + versoRacchetta * V_RACCHETTA * PASSO, RACCHETTA_W / 2, LARGHEZZA - RACCHETTA_W / 2);
        if (incollata) {
            pos = new Vec2(racchettaX, RACCHETTA_H + RAGGIO);
            return;
        }
        int sottopassi = Math.max(1, (int) Math.ceil(vel.modulo() * PASSO / (RAGGIO / 2)));   // contro il tunneling
        double dt = PASSO / sottopassi;
        for (int i = 0; i < sottopassi; i++) {
            pos = pos.piu(vel.per(dt));
            parete(new Vec2(1, 0), pos.x() - RAGGIO);
            parete(new Vec2(-1, 0), LARGHEZZA - pos.x() - RAGGIO);
            parete(new Vec2(0, -1), ALTEZZA - pos.y() - RAGGIO);
            racchetta();
            for (Mattone m : mattoni)                     // si scorre, non si modifica: i rotti si tolgono dopo
                if (!m.rotto() && ostacolo(m.forma())) {
                    m.colpito();
                    if (m.rotto()) {
                        rottiNelPasso.add(m);
                        break;                            // un mattone per sottopasso
                    }
                }
        }
        if (!rottiNelPasso.isEmpty()) {                   // a fine passo: via i rotti, annunci
            mattoni.removeAll(rottiNelPasso);
            for (Mattone m : rottiNelPasso) {
                setPunteggio(punteggio + m.tipo().punti);
                for (Listener l : List.copyOf(listeners))
                    l.mattoneRotto(m, m.tipo().punti);
            }
            rottiNelPasso.clear();
            if (mattoni.stream().allMatch(m -> m.tipo() == Mattone.Tipo.ACCIAIO))
                for (Listener l : List.copyOf(listeners))
                    l.livelloFinito();
        }
        if (pos.y() < -RAGGIO) {                          // persa
            setVite(vite - 1);
            incolla();
            for (Listener l : List.copyOf(listeners))
                l.pallinaPersa();
        }
    }

    private void parete(Vec2 n, double gap) {
        if (gap >= 0)
            return;
        pos = pos.meno(n.per(gap));
        double vn = vel.scalare(n);
        if (vn < 0) {
            vel = vel.meno(n.per(2 * vn));
            annunciaRimbalzo();
        }
    }

    private boolean ostacolo(Ostacolo o) {
        Vec2 q = o.puntoPiuVicino(pos);
        Vec2 d = pos.meno(q);
        double dist = d.modulo();
        if (dist >= RAGGIO)
            return false;
        Vec2 n = dist == 0 ? new Vec2(0, -1) : d.versore();
        pos = pos.piu(n.per(RAGGIO - dist));
        double vn = vel.scalare(n);
        if (vn < 0) {
            vel = vel.meno(n.per(2 * vn));
            annunciaRimbalzo();
            return true;
        }
        return false;
    }

    /** La racchetta: il punto dell'impatto decide l'angolo (ai bordi piu inclinato). */
    private void racchetta() {
        Ostacolo r = new Ostacolo(racchettaX - RACCHETTA_W / 2, 0, RACCHETTA_W, RACCHETTA_H);
        Vec2 q = r.puntoPiuVicino(pos);
        if (pos.meno(q).modulo() >= RAGGIO || vel.y() >= 0)
            return;
        double t = Math.clamp((pos.x() - racchettaX) / (RACCHETTA_W / 2), -1, 1);   // -1 .. 1
        double angolo = Math.toRadians(90 - 60 * t);                                  // 30 .. 150 gradi
        vel = new Vec2(Math.cos(angolo), Math.sin(angolo)).per(Math.min(14, vel.modulo() * 1.02));
        pos = new Vec2(pos.x(), RACCHETTA_H + RAGGIO);
        annunciaRimbalzo();
    }

    private void annunciaRimbalzo() {
        for (Listener l : List.copyOf(listeners))
            l.rimbalzo();
    }
}
