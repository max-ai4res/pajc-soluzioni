package it.unibs.pajc.redolfi.e08;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il mondo di Palline, quinta versione: come la quarta, ma le particelle vengono da un
 * pool a dimensione fissa invece di essere create a ogni schizzo.
 */
public class Mondo {

    public static final double PASSO = 1.0 / 200;
    private static final double DT_MASSIMO = 0.1;
    private static final double SOGLIA_QUIETE = 0.15;
    private static final double ATTRITO = 0.3;

    private final double larghezza, altezza;
    private final List<Pallina> palline = new ArrayList<>();
    private final PoolParticelle particelle = new PoolParticelle(400);
    private final List<Ostacolo> ostacoli = new ArrayList<>();
    private double gravita = 9.81;
    private double accumulatore, tempo;
    private long passi;

    private final List<MondoListener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);
    private final List<Rimbalzo> rimbalzi = new ArrayList<>();
    private final List<Pallina> fermate = new ArrayList<>(), scoppiate = new ArrayList<>();

    public Mondo(double larghezza, double altezza) {
        this.larghezza = larghezza;
        this.altezza = altezza;
    }

    public double larghezza() { return larghezza; }
    public double altezza()   { return altezza; }
    public double tempo()     { return tempo; }
    public long passi()       { return passi; }
    public List<Pallina> palline()       { return Collections.unmodifiableList(palline); }
    public List<Particella> particelle() { return particelle.tutte(); }
    public List<Ostacolo> ostacoli()     { return Collections.unmodifiableList(ostacoli); }

    public void addMondoListener(MondoListener l)    { listeners.add(l); }
    public void removeMondoListener(MondoListener l) { listeners.remove(l); }
    public void addPropertyChangeListener(PropertyChangeListener l)    { proprieta.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { proprieta.removePropertyChangeListener(l); }

    public double gravita() { return gravita; }

    public void setGravita(double g) {
        double vecchia = gravita;
        gravita = g;
        for (Pallina p : palline)
            if (p.ferma())
                p.setVelocita(Vec2.ZERO);
        proprieta.firePropertyChange("gravita", vecchia, g);
    }

    public void aggiungi(Ostacolo o) {
        ostacoli.add(o);
    }

    public Pallina lascia(Vec2 punto, double raggio, Materiale materiale) {
        Pallina p = new Pallina(punto, raggio, materiale);
        palline.add(p);
        for (MondoListener l : List.copyOf(listeners))
            l.pallinaLasciata(p);
        return p;
    }

    /** Toglie l'ultima pallina lasciata, se c'e': serve ad annullare. */
    public void rimuoviUltima() {
        if (!palline.isEmpty())
            palline.remove(palline.size() - 1);
    }

    public void svuota() {
        palline.clear();
        particelle.spegniTutte();
    }

    /** Rimette una lista di palline: serve ad annullare uno svuotamento. */
    public void ripristina(List<Pallina> salvate) {
        palline.clear();
        palline.addAll(salvate);
    }

    public double energia() {
        double e = 0;
        for (Pallina p : palline)
            e += p.energia(gravita);
        return e;
    }

    // ------------------------------------------------------------------ il tempo

    public void stepNext(double dt) {
        accumulatore += Math.min(dt, DT_MASSIMO);
        while (accumulatore >= PASSO - 1e-12) {
            passoFisso();
            accumulatore -= PASSO;
        }
        for (MondoListener l : List.copyOf(listeners)) {
            for (Rimbalzo r : rimbalzi)
                l.rimbalzo(r);
            for (Pallina p : fermate)
                l.pallinaFerma(p);
            for (Pallina p : scoppiate)
                l.pallinaScoppiata(p);
            l.avanzato(tempo);
        }
        rimbalzi.clear();
        fermate.clear();
        scoppiate.clear();
    }

    private void passoFisso() {
        tempo += PASSO;
        passi++;
        double vMax = 0, rMin = Double.MAX_VALUE;
        for (Pallina p : palline) {
            vMax = Math.max(vMax, p.velocita().modulo());
            rMin = Math.min(rMin, p.raggio());
        }
        int sottopassi = Math.max(1, (int) Math.ceil(vMax * PASSO / (rMin / 2)));
        double dt = PASSO / sottopassi;
        Vec2 g = new Vec2(0, -gravita);
        for (int i = 0; i < sottopassi; i++) {
            for (Pallina p : palline)
                if (!p.ferma())
                    p.stepNext(dt, g);                     // il Template Method
            for (int a = 0; a < palline.size(); a++)
                for (int b = a + 1; b < palline.size(); b++)
                    urto(palline.get(a), palline.get(b));
            for (Pallina p : palline) {
                Vec2 x = p.posizione();
                contatto(p, new Vec2(0, 1), x.y() - p.raggio(), true);
                contatto(p, new Vec2(1, 0), x.x() - p.raggio(), false);
                contatto(p, new Vec2(-1, 0), larghezza - x.x() - p.raggio(), false);
                for (Ostacolo o : ostacoli) {
                    Vec2 d = p.posizione().meno(o.puntoPiuVicino(p.posizione()));
                    double dist = d.modulo();
                    if (dist < p.raggio()) {
                        Vec2 n = dist == 0 ? new Vec2(0, 1) : d.versore();
                        contatto(p, n, dist - p.raggio(), n.y() > 0.7);
                    }
                }
            }
        }
        for (Particella s : particelle.tutte())
            if (s.viva())
                s.stepNext(PASSO, g);
        palline.removeIf(p -> !p.viva());                // chi e' scoppiato se ne va
    }

    // ------------------------------------------------------------------ gli urti

    private void contatto(Pallina p, Vec2 n, double gap, boolean appoggio) {
        if (gap >= 0)
            return;
        p.setPosizione(p.posizione().meno(n.per(gap)));
        double vn = p.velocita().scalare(n);
        if (vn >= 0)
            return;
        Vec2 tangente = p.velocita().meno(n.per(vn));
        Vec2 nuova = tangente.per(1 - ATTRITO).meno(n.per(vn * p.restituzione()));
        if (!p.materiale().sopravvive(-vn)) {            // la Strategy decide
            p.scoppia();
            scoppiate.add(p);
            schizzo(p.posizione(), n, -vn, 16);
            return;
        }
        if (appoggio && nuova.modulo() < SOGLIA_QUIETE) {
            p.fermati();
            fermate.add(p);
        } else {
            p.setVelocita(nuova);
            if (-vn > SOGLIA_QUIETE) {
                rimbalzi.add(new Rimbalzo(p, -vn));
                schizzo(p.posizione().meno(n.per(p.raggio())), n, -vn, 6);
            }
        }
    }

    /** Lo schizzo di un urto: n schegge che partono dal punto, attorno alla normale. */
    private void schizzo(Vec2 punto, Vec2 n, double impatto, int quante) {
        for (int i = 0; i < quante; i++) {
            double angolo = Math.atan2(n.y(), n.x()) + (i - (quante - 1) / 2.0) * 0.25;
            Vec2 v = new Vec2(Math.cos(angolo), Math.sin(angolo)).per(0.3 * impatto);
            particelle.prendi(punto, v);                 // dal pool: nessun new qui
        }
    }

    private void urto(Pallina a, Pallina b) {
        Vec2 d = b.posizione().meno(a.posizione());
        double dist = d.modulo(), somma = a.raggio() + b.raggio();
        if (dist >= somma)
            return;
        Vec2 n = dist == 0 ? new Vec2(1, 0) : d.per(1 / dist);
        double penetrazione = somma - dist, mTot = a.massa() + b.massa();
        a.setPosizione(a.posizione().meno(n.per(penetrazione * b.massa() / mTot)));
        b.setPosizione(b.posizione().piu(n.per(penetrazione * a.massa() / mTot)));
        double vn = b.velocita().meno(a.velocita()).scalare(n);
        if (vn >= 0)
            return;
        double e = Math.min(a.restituzione(), b.restituzione());
        double j = -(1 + e) * vn / (1 / a.massa() + 1 / b.massa());
        a.setVelocita(a.velocita().meno(n.per(j / a.massa())));
        b.setVelocita(b.velocita().piu(n.per(j / b.massa())));
        rimbalzi.add(new Rimbalzo(a, -vn));
        for (Pallina p : List.of(a, b))
            if (!p.materiale().sopravvive(-vn)) {
                p.scoppia();
                scoppiate.add(p);
                schizzo(p.posizione(), n, -vn, 16);
            }
    }
}
