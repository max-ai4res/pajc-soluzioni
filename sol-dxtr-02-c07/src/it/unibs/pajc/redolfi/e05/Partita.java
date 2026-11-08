package it.unibs.pajc.redolfi.e05;

import java.util.ArrayList;
import java.util.List;

/**
 * Problema 5, il biliardo con le buche: la partita a turni sopra il tavolo.
 *
 * Idea: il tavolo e' il Mondo del capitolo 4 con sei buche controllate a passo completo,
 * prima delle notifiche (una pallina tolta a meta' sottopasso lascerebbe l'altra della
 * coppia con un impulso a meta'). La partita ha gli stati TurnoDi, InMoto e Finita; il
 * colpo e' un comando registrato con il tempo del mondo e "Rivedi" lo rifa' da un
 * Memento delle palline, con la fisica deterministica.
 *
 * Errore tipico: la buca come ostacolo circolare (respinge invece di inghiottire), o il
 * controllo nel sottopasso.
 */
public class Partita implements MondoListener {

    public record Colpo(double tempo, Vec2 velocita) {
    }

    public interface Listener {
        default void turno(int giocatore) { }
        default void punteggio(int a, int b) { }
        default void finita(int vincitore) { }
    }

    public interface Stato {
        default void stepNext(Partita p, double dt) { }
        default boolean colpo(Partita p, Vec2 v) { return false; }
        String nome();
    }

    class TurnoDi implements Stato {
        public boolean colpo(Partita p, Vec2 v) {
            salvaMemento();
            ultimoColpo = new Colpo(mondo.tempo(), v);
            mondo.colpisci(bianca, v);
            cambia(new InMoto());
            return true;
        }
        public String nome() { return "tira il giocatore " + (giocatore + 1); }
    }

    class InMoto implements Stato {
        public void stepNext(Partita p, double dt) {
            mondo.stepNext(dt);
            if (mondo.tutteFerme()) {
                if (!mondo.palline().contains(bianca)) {       // la bianca e' finita in buca: torna al via
                    mondo.rimetti(bianca, PARTENZA);
                    giocatore = 1 - giocatore;
                } else if (!imbucataNelColpo) {
                    giocatore = 1 - giocatore;
                }
                imbucataNelColpo = false;
                if (mondo.palline().size() == 1)
                    cambia(new Finita());
                else
                    cambia(new TurnoDi());
            }
        }
        public String nome() { return "in moto"; }
    }

    class Finita implements Stato {
        public String nome() { return "vince il giocatore " + (punti[0] > punti[1] ? 1 : 2); }
    }

    static final Vec2 PARTENZA = new Vec2(0.7, 0.71);

    private Mondo mondo;
    private Pallina bianca;
    private Stato stato;
    private int giocatore;
    private final int[] punti = new int[2];
    private boolean imbucataNelColpo;
    private Colpo ultimoColpo;
    private List<Pallina> memento;
    private final List<Listener> listeners = new ArrayList<>();

    public Partita() {
        mondo = nuovoTavolo();
        cambia(new TurnoDi());
    }

    public void addListener(Listener l) { listeners.add(l); }
    public Mondo mondo()       { return mondo; }
    public Pallina bianca()    { return bianca; }
    public Stato stato()       { return stato; }
    public int[] punti()       { return punti.clone(); }
    public int giocatore()     { return giocatore; }

    private Mondo nuovoTavolo() {
        Mondo m = new Mondo(2.84, 1.42);
        for (double x : new double[] { 0, 1.42, 2.84 })
            for (double y : new double[] { 0, 1.42 })
                if (x != 1.42 || true)
                    m.aggiungi(new Vec2(x, y));
        double r = 0.03;
        bianca = m.lascia(PARTENZA, r, 0.95);
        for (int fila = 0; fila < 4; fila++)
            for (int k = 0; k <= fila && m.palline().size() < 8; k++)
                m.lascia(new Vec2(1.9 + fila * 2 * r * 0.87, 0.71 + (k - fila / 2.0) * 2 * r * 1.02), r, 0.95);
        for (Pallina p : m.palline())
            p.setVelocita(Vec2.ZERO);
        m.addMondoListener(this);
        return m;
    }

    private void cambia(Stato nuovo) {
        stato = nuovo;
        for (Listener l : List.copyOf(listeners))
            l.turno(giocatore);
        if (nuovo instanceof Finita)
            for (Listener l : List.copyOf(listeners))
                l.finita(punti[0] > punti[1] ? 1 : 2);
    }

    @Override
    public void imbucata(Pallina p) {
        if (p != bianca) {
            punti[giocatore]++;
            imbucataNelColpo = true;
            for (Listener l : List.copyOf(listeners))
                l.punteggio(punti[0], punti[1]);
        }
    }

    public void stepNext(double dt) { stato.stepNext(this, dt); }

    public boolean colpo(Vec2 v) { return stato.colpo(this, v); }

    /** Il Memento: le palline com'erano prima del colpo, con posizione e velocita (ferme). */
    private void salvaMemento() {
        memento = new ArrayList<>();
        for (Pallina p : mondo.palline())
            memento.add(copia(p));
    }

    private Pallina copia(Pallina p) {
        Pallina c = new Pallina(p.posizione(), p.raggio(), p.restituzione());
        c.fermati();
        return c;
    }

    /** Rivedi: il tavolo torna com'era e lo stesso colpo viene rieseguito. Stessa fisica, stessa scena. */
    public void rivedi() {
        if (memento == null || ultimoColpo == null || !(stato instanceof TurnoDi || stato instanceof Finita))
            return;
        Mondo m = new Mondo(2.84, 1.42);
        for (Vec2 b : mondo.buche())
            m.aggiungi(b);
        Pallina nuovaBianca = null;
        for (Pallina p : memento) {
            Pallina q = m.lascia(p.posizione(), p.raggio(), p.restituzione());
            q.setVelocita(Vec2.ZERO);
            if (p.posizione().equals(bianca.posizione()) && nuovaBianca == null)
                nuovaBianca = q;
        }
        if (nuovaBianca == null)
            nuovaBianca = m.palline().get(0);
        m.addMondoListener(this);
        mondo = m;
        bianca = nuovaBianca;
        giocatore = 1 - giocatore;                             // il colpo e' di chi lo aveva tirato
        punti[giocatore] = Math.max(0, punti[giocatore]);      // i punti del colpo rivisto non si contano due volte
        rivedendo = true;
        mondo.colpisci(bianca, ultimoColpo.velocita());
        cambia(new InMoto());
    }

    private boolean rivedendo;

    public boolean rivedendo() { return rivedendo; }
}
