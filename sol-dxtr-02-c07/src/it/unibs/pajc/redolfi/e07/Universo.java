package it.unibs.pajc.redolfi.e07;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Il modello di Asteroidi: la navicella, gli asteroidi che si spezzano, i proiettili dal
 * pool, le collisioni decise con i cerchi, l'universo toroidale, punteggio e vite come
 * proprieta'.
 */
public class Universo {

    public interface Listener {
        default void asteroideColpito(Asteroide a) { }
        default void navicellaColpita() { }
        default void sparo() { }
    }

    public static final double LARGHEZZA = 24, ALTEZZA = 16, PASSO = 1.0 / 120;

    private final Navicella navicella = new Navicella(new Vec2(LARGHEZZA / 2, ALTEZZA / 2));
    private final List<Asteroide> asteroidi = new ArrayList<>();
    private final List<Proiettile> pool = new ArrayList<>();
    private final Random random;
    private int punteggio, vite = 3;
    private double accumulatore;
    private final List<Listener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public Universo(long seme) {
        random = new Random(seme);
        for (int i = 0; i < 24; i++)
            pool.add(new Proiettile());
        for (int i = 0; i < 5; i++)
            asteroidi.add(nuovoAsteroide());
    }

    public void addListener(Listener l) { listeners.add(l); }
    public void addPropertyChangeListener(PropertyChangeListener l) { proprieta.addPropertyChangeListener(l); }

    public Navicella navicella()        { return navicella; }
    public List<Asteroide> asteroidi()  { return Collections.unmodifiableList(asteroidi); }
    public List<Proiettile> proiettili() { return Collections.unmodifiableList(pool); }
    public int punteggio()              { return punteggio; }
    public int vite()                   { return vite; }

    private Asteroide nuovoAsteroide() {
        double x, y;
        do {
            x = random.nextDouble() * LARGHEZZA;
            y = random.nextDouble() * ALTEZZA;
        } while (new Vec2(x, y).meno(navicella.posizione()).modulo() < 5);
        double a = random.nextDouble() * 2 * Math.PI;
        return new Asteroide(new Vec2(x, y), new Vec2(Math.cos(a), Math.sin(a)).per(1 + random.nextDouble()),
                Asteroide.Taglia.GRANDE, random.nextDouble() - 0.5);
    }

    public boolean spara() {
        for (Proiettile p : pool)
            if (!p.viva()) {
                Vec2 dir = new Vec2(Math.cos(navicella.angolo()), Math.sin(navicella.angolo()));
                p.spara(navicella.posizione().piu(dir.per(0.7)), navicella.velocita().piu(dir.per(16)));
                for (Listener l : List.copyOf(listeners))
                    l.sparo();
                return true;
            }
        return false;
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
        navicella.stepNext(PASSO, LARGHEZZA, ALTEZZA);
        for (Asteroide a : asteroidi)
            a.stepNext(PASSO, LARGHEZZA, ALTEZZA);
        for (Proiettile p : pool)
            if (p.viva())
                p.stepNext(PASSO, LARGHEZZA, ALTEZZA);
        List<Asteroide> nuovi = new ArrayList<>();
        for (Asteroide a : asteroidi) {
            if (!a.viva())
                continue;
            for (Proiettile p : pool)
                if (p.viva() && p.tocca(a)) {
                    p.spegni();
                    a.colpisci();
                    setPunteggio(punteggio + a.taglia().punti);
                    nuovi.addAll(frammenti(a));
                    for (Listener l : List.copyOf(listeners))
                        l.asteroideColpito(a);
                    break;
                }
            if (a.viva() && !navicella.invulnerabile() && a.tocca(navicella)) {
                setVite(vite - 1);
                navicella.rinasci(new Vec2(LARGHEZZA / 2, ALTEZZA / 2));
                for (Listener l : List.copyOf(listeners))
                    l.navicellaColpita();
            }
        }
        asteroidi.removeIf(a -> !a.viva());             // a fine passo
        asteroidi.addAll(nuovi);
        if (asteroidi.isEmpty())
            for (int i = 0; i < 6; i++)
                asteroidi.add(nuovoAsteroide());
    }

    private List<Asteroide> frammenti(Asteroide a) {
        List<Asteroide> f = new ArrayList<>();
        Asteroide.Taglia prossima = switch (a.taglia()) {
            case GRANDE -> Asteroide.Taglia.MEDIO;
            case MEDIO -> Asteroide.Taglia.PICCOLO;
            case PICCOLO -> null;
        };
        if (prossima != null)
            for (int i = 0; i < 2; i++) {
                double ang = random.nextDouble() * 2 * Math.PI;
                f.add(new Asteroide(a.posizione(), a.velocita().piu(new Vec2(Math.cos(ang), Math.sin(ang)).per(1.5)),
                        prossima, random.nextDouble() * 2 - 1));
            }
        return f;
    }
}
