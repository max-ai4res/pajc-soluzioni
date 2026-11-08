package it.unibs.pajc.redolfi.e04;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Il mondo di Palline, seconda versione: annuncia cio' che gli succede e ha due proprieta. */
public class Mondo {

    private static final double SOGLIA_QUIETE = 0.5;     // m/s: sotto, la pallina si ferma

    private final double larghezza, altezza;
    private final List<Pallina> palline = new ArrayList<>();
    private double gravita = 9.81;                       // m/s^2, in giu
    private boolean inPausa;
    private double tempo;                                // secondi di mondo, dall'inizio

    private final List<MondoListener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public Mondo(double larghezza, double altezza) {
        this.larghezza = larghezza;
        this.altezza = altezza;
    }

    public double tempo()     { return tempo; }
    public double larghezza() { return larghezza; }
    public double altezza()   { return altezza; }

    public List<Pallina> palline() {
        return Collections.unmodifiableList(palline);
    }

    // ---- i listener degli eventi

    public void addMondoListener(MondoListener l)    { listeners.add(l); }
    public void removeMondoListener(MondoListener l) { listeners.remove(l); }

    // ---- le proprieta

    public void addPropertyChangeListener(PropertyChangeListener l) {
        proprieta.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        proprieta.removePropertyChangeListener(l);
    }

    public double gravita() { return gravita; }

    public void setGravita(double g) {
        double vecchia = gravita;
        gravita = g;
        proprieta.firePropertyChange("gravita", vecchia, g);
    }

    public boolean inPausa() { return inPausa; }

    public void setInPausa(boolean pausa) {
        boolean vecchia = inPausa;
        inPausa = pausa;
        proprieta.firePropertyChange("inPausa", vecchia, pausa);
    }

    // ---- le operazioni

    public void lascia(Vec2 punto, double raggio) {
        Pallina p = new Pallina(punto, raggio);
        palline.add(p);
        for (MondoListener l : List.copyOf(listeners))
            l.pallinaLasciata(p);
    }

    public void stepNext(double dt) {
        if (inPausa)
            return;
        tempo += dt;
        Vec2 g = new Vec2(0, -gravita);
        List<Rimbalzo> rimbalzi = new ArrayList<>();
        List<Pallina> fermate = new ArrayList<>();
        for (Pallina p : palline) {
            if (p.ferma())
                continue;
            p.stepNext(dt, g);
            if (p.posizione().y() < p.raggio()) {
                double impatto = -p.velocita().y();
                p.rimbalzaSulPavimento(0.8);
                if (p.velocita().y() < SOGLIA_QUIETE) {
                    p.fermati();
                    fermate.add(p);
                } else {
                    rimbalzi.add(new Rimbalzo(p, impatto));
                }
            }
        }
        // si annuncia alla fine, a passo concluso: lo stato che i listener leggono e' coerente
        for (MondoListener l : List.copyOf(listeners)) {
            for (Rimbalzo r : rimbalzi)
                l.rimbalzo(r);
            for (Pallina p : fermate)
                l.pallinaFerma(p);
        }
    }
}
