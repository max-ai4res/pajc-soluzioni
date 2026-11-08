package it.unibs.pajc.redolfi.e02;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il mondo di Palline, terza versione: passo fisso con l'accumulatore, Eulero
 * semi-implicito, rimbalzi con la correzione della penetrazione e la restituzione, urti
 * fra palline con l'impulso, pareti e ostacoli rettangolari, sottopassi contro il
 * tunneling, quiete.
 */
public class Mondo {

    public static final double PASSO = 1.0 / 200;        // s: il passo fisso della fisica
    private static final double DT_MASSIMO = 0.1;        // s: oltre, il tempo in piu si butta
    private static final double SOGLIA_QUIETE = 0.15;    // m/s
    private static final double ATTRITO = 0.3;           // frazione di velocita tangenziale persa a ogni urto

    private final double larghezza, altezza;
    private final List<Pallina> palline = new ArrayList<>();
    private final List<Ostacolo> ostacoli = new ArrayList<>();
    private final List<Rampa> rampe = new ArrayList<>();
    private static final double ATTRITO_ROTOLAMENTO = 0.4;   // frazione di velocita persa al secondo sulla rampa
    private double gravita = 9.81;
    private boolean inPausa;
    private double accumulatore;                         // tempo ricevuto e non ancora simulato
    private double tempo;                                // s di mondo simulati
    private long passi;

    private final List<MondoListener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);
    private final List<Rimbalzo> rimbalzi = new ArrayList<>();
    private final List<Pallina> fermate = new ArrayList<>();

    public Mondo(double larghezza, double altezza) {
        this.larghezza = larghezza;
        this.altezza = altezza;
    }

    public double larghezza() { return larghezza; }
    public double altezza()   { return altezza; }
    public double tempo()     { return tempo; }
    public long passi()       { return passi; }
    public List<Pallina> palline()  { return Collections.unmodifiableList(palline); }
    public List<Ostacolo> ostacoli() { return Collections.unmodifiableList(ostacoli); }
    public List<Rampa> rampe()       { return Collections.unmodifiableList(rampe); }

    public void addMondoListener(MondoListener l)    { listeners.add(l); }
    public void removeMondoListener(MondoListener l) { listeners.remove(l); }
    public void addPropertyChangeListener(PropertyChangeListener l)    { proprieta.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { proprieta.removePropertyChangeListener(l); }

    public double gravita() { return gravita; }

    public void setGravita(double g) {
        double vecchia = gravita;
        gravita = g;
        for (Pallina p : palline)                        // cambia la gravita: chi dorme si sveglia
            if (p.ferma())
                p.setVelocita(Vec2.ZERO);
        proprieta.firePropertyChange("gravita", vecchia, g);
    }

    public boolean inPausa() { return inPausa; }

    public void setInPausa(boolean pausa) {
        boolean vecchia = inPausa;
        inPausa = pausa;
        proprieta.firePropertyChange("inPausa", vecchia, pausa);
    }

    public void aggiungi(Ostacolo o) {
        ostacoli.add(o);
    }

    public void aggiungi(Rampa r) {
        rampe.add(r);
    }

    public void lascia(Vec2 punto, double raggio) {
        lascia(punto, raggio, 0.8);
    }

    public Pallina lascia(Vec2 punto, double raggio, double restituzione) {
        Pallina p = new Pallina(punto, raggio, restituzione);
        palline.add(p);
        for (MondoListener l : List.copyOf(listeners))
            l.pallinaLasciata(p);
        return p;
    }

    /** Lascia una pallina con una velocita iniziale. */
    public Pallina lancia(Vec2 punto, double raggio, Vec2 velocita) {
        Pallina p = lascia(punto, raggio, 0.8);
        p.setVelocita(velocita);
        return p;
    }

    /** L'energia meccanica totale, per i test e per chi la vuole mostrare. */
    public double energia() {
        double e = 0;
        for (Pallina p : palline)
            e += p.energia(gravita);
        return e;
    }

    // ------------------------------------------------------------------ il tempo

    /** Riceve il tempo trascorso e simula tanti passi fissi quanti ce ne stanno. */
    public void stepNext(double dt) {
        if (inPausa)
            return;
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
        }
        rimbalzi.clear();
        fermate.clear();
    }

    /** Un passo di PASSO secondi, diviso in sottopassi se qualcuno corre troppo. */
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
        for (int i = 0; i < sottopassi; i++) {
            Vec2 g = new Vec2(0, -gravita);
            for (Pallina p : palline) {
                if (p.ferma())
                    continue;
                p.setVelocita(p.velocita().piu(g.per(dt)));      // prima v...
                p.setPosizione(p.posizione().piu(p.velocita().per(dt)));   // ...poi x
            }
            for (int a = 0; a < palline.size(); a++)
                for (int b = a + 1; b < palline.size(); b++)
                    urto(palline.get(a), palline.get(b));
            for (Pallina p : palline) {                  // anche chi dorme: un urto puo averla spostata
                Vec2 x = p.posizione();
                contatto(p, new Vec2(0, 1), x.y() - p.raggio(), true);        // pavimento
                contatto(p, new Vec2(1, 0), x.x() - p.raggio(), false);       // sinistra
                contatto(p, new Vec2(-1, 0), larghezza - x.x() - p.raggio(), false);
                for (Ostacolo o : ostacoli) {
                    Vec2 q = o.puntoPiuVicino(p.posizione());
                    Vec2 d = p.posizione().meno(q);
                    double dist = d.modulo();
                    if (dist < p.raggio()) {
                        Vec2 n = dist == 0 ? new Vec2(0, 1) : d.versore();
                        contatto(p, n, dist - p.raggio(), n.y() > 0.7);
                    }
                }
                for (Rampa r : rampe) {
                    Vec2 q = r.puntoPiuVicino(p.posizione());
                    Vec2 d = p.posizione().meno(q);
                    double dist = d.modulo();
                    if (dist < p.raggio() && !p.ferma()) {
                        // la rampa frena per unita di tempo, non per urto: altrimenti il rallentamento dipende dal passo
                        p.setVelocita(p.velocita().per(1 - ATTRITO_ROTOLAMENTO * dt));
                        Vec2 n = dist == 0 ? new Vec2(0, 1) : d.versore();
        contatto(p, n, dist - p.raggio(), n.y() > 0.7);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ gli urti

    /** Contatto con una superficie fissa: n la normale (verso la pallina), gap la distanza (negativa se penetra). */
    private void contatto(Pallina p, Vec2 n, double gap, boolean appoggio) {
        if (gap >= 0)
            return;
        p.setPosizione(p.posizione().meno(n.per(gap)));            // 1. fuori
        double vn = p.velocita().scalare(n);
        if (vn >= 0)
            return;                                                // si sta gia allontanando
        Vec2 tangente = p.velocita().meno(n.per(vn));              // 2. scomposta
        Vec2 nuova = tangente.per(1 - ATTRITO).meno(n.per(vn * p.restituzione()));
        if (appoggio && nuova.modulo() < SOGLIA_QUIETE) {          // 3. quiete
            p.fermati();
            fermate.add(p);
        } else {
            p.setVelocita(nuova);
            if (-vn > SOGLIA_QUIETE)
                rimbalzi.add(new Rimbalzo(p, -vn));
        }
    }

    /** Urto fra due palline: separate, poi l'impulso lungo la normale. */
    private void urto(Pallina a, Pallina b) {
        Vec2 d = b.posizione().meno(a.posizione());
        double dist = d.modulo(), somma = a.raggio() + b.raggio();
        if (dist >= somma)
            return;
        Vec2 n = dist == 0 ? new Vec2(1, 0) : d.per(1 / dist);
        double penetrazione = somma - dist, mTot = a.massa() + b.massa();
        a.setPosizione(a.posizione().meno(n.per(penetrazione * b.massa() / mTot)));
        b.setPosizione(b.posizione().piu(n.per(penetrazione * a.massa() / mTot)));
        double vn = b.velocita().meno(a.velocita()).scalare(n);   // velocita relativa lungo n
        if (vn >= 0)
            return;                                                // si stanno gia separando
        double e = Math.min(a.restituzione(), b.restituzione());
        double j = -(1 + e) * vn / (1 / a.massa() + 1 / b.massa());
        a.setVelocita(a.velocita().meno(n.per(j / a.massa())));
        b.setVelocita(b.velocita().piu(n.per(j / b.massa())));
        rimbalzi.add(new Rimbalzo(a, -vn));
    }
}
