package it.unibs.pajc.redolfi.e03;

import java.util.ArrayList;
import java.util.List;

/** Il modello: le voci e le tre operazioni, ciascuna annunciata a strutture gia' aggiornate. */
public class Spesa {

    private final List<Voce> voci = new ArrayList<>();
    private final List<SpesaListener> listeners = new ArrayList<>();

    public void addSpesaListener(SpesaListener l)    { listeners.add(l); }
    public void removeSpesaListener(SpesaListener l) { listeners.remove(l); }

    public int quante()        { return voci.size(); }
    public Voce voce(int i)    { return voci.get(i); }

    public double totaleDaPrendere() {
        return voci.stream().filter(v -> !v.presa()).mapToDouble(Voce::prezzo).sum();
    }

    public void aggiungi(String nome, double prezzo) {
        Voce v = new Voce(nome, prezzo, false);
        voci.add(v);
        int i = voci.size() - 1;
        for (SpesaListener l : List.copyOf(listeners))
            l.aggiunta(i, v);
    }

    public void rimuovi(int i) {
        Voce v = voci.remove(i);
        for (SpesaListener l : List.copyOf(listeners))
            l.rimossa(i, v);
    }

    public void segna(int i, boolean presa) {
        Voce v = new Voce(voci.get(i).nome(), voci.get(i).prezzo(), presa);
        voci.set(i, v);
        for (SpesaListener l : List.copyOf(listeners))
            l.cambiata(i, v);
    }
}
