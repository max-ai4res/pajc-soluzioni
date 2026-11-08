package it.unibs.pajc.redolfi.e04;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Il modello: la griglia, il serpente come coda di celle (testa in cima), la mela, il
 * passo ogni T secondi di tempo ricevuto, la coda delle direzioni in attesa. Niente Swing.
 */
public class Serpente {

    public interface Listener {
        default void passoFatto() { }
        default void melaMangiata(int punti) { }
        default void partitaFinita(int punti) { }
    }

    private final int righe, colonne;
    private final Deque<Cella> corpo = new ArrayDeque<>();
    private final Deque<Direzione> inAttesa = new ArrayDeque<>();
    private Direzione direzione = Direzione.DESTRA;
    private Cella mela;
    private double periodo = 0.25, accumulato;
    private int daCrescere, punti;
    private boolean finita;
    private final Random random;
    private final List<Listener> listeners = new ArrayList<>();

    public Serpente(int righe, int colonne, long seme) {
        this.righe = righe;
        this.colonne = colonne;
        this.random = new Random(seme);
        for (int i = 0; i < 3; i++)
            corpo.addLast(new Cella(righe / 2, colonne / 2 - i));
        nuovaMela();
    }

    public void addListener(Listener l) { listeners.add(l); }

    public int righe()             { return righe; }
    public int colonne()           { return colonne; }
    public Iterable<Cella> corpo() { return corpo; }
    public Cella testa()           { return corpo.peekFirst(); }
    public Cella mela()            { return mela; }
    public int punti()             { return punti; }
    public boolean finita()        { return finita; }

    /** Il comando: la direzione si applica al prossimo passo, in coda a quelle gia' premute. */
    public void gira(Direzione d) {
        Direzione ultima = inAttesa.isEmpty() ? direzione : inAttesa.peekLast();
        if (d != ultima && !d.oppostaA(ultima) && inAttesa.size() < 2)
            inAttesa.addLast(d);
    }

    public void stepNext(double dt) {
        if (finita)
            return;
        accumulato += dt;
        while (accumulato >= periodo && !finita) {
            accumulato -= periodo;
            stepNext();
        }
    }

    void stepNext() {
        if (!inAttesa.isEmpty())
            direzione = inAttesa.pollFirst();
        Cella nuova = testa().piu(direzione);
        boolean fuori = nuova.riga() < 0 || nuova.riga() >= righe || nuova.colonna() < 0 || nuova.colonna() >= colonne;
        if (fuori || corpo.contains(nuova)) {
            finita = true;
            for (Listener l : List.copyOf(listeners))
                l.partitaFinita(punti);
            return;
        }
        corpo.addFirst(nuova);
        if (nuova.equals(mela)) {
            punti += 10;
            daCrescere += 2;
            periodo = Math.max(0.08, periodo * 0.95);
            nuovaMela();
            for (Listener l : List.copyOf(listeners))
                l.melaMangiata(punti);
        }
        if (daCrescere > 0)
            daCrescere--;
        else
            corpo.removeLast();
        for (Listener l : List.copyOf(listeners))
            l.passoFatto();
    }

    private void nuovaMela() {
        do
            mela = new Cella(random.nextInt(righe), random.nextInt(colonne));
        while (corpo.contains(mela));
    }
}
