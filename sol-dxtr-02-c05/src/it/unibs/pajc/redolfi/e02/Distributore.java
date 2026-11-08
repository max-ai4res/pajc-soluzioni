package it.unibs.pajc.redolfi.e02;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Esercizio 5.2, il distributore automatico.
 *
 * Idea: quattro stati come classi, i comandi come record di un'interfaccia sealed; ogni
 * stato fa uno switch sui comandi (sono pochi e chiusi) e decide. Il modello non importa
 * Swing: annuncia con un DistributoreListener, e la vista ascolta. ProvaDistributore
 * manda una sequenza e verifica stati e credito.
 *
 * Errore tipico: uno stato che scrive nelle etichette. Qui gli stati parlano solo con il
 * modello, e il modello annuncia.
 */
public class Distributore {

    static final Map<String, Integer> PREZZI = Map.of("acqua", 80, "caffe", 100, "succo", 150);
    static final double DURATA_EROGAZIONE = 3.0;

    private Stato stato;
    private int credito;
    private final List<DistributoreListener> listeners = new ArrayList<>();

    public Distributore() {
        cambia(new InAttesa());
    }

    public void addDistributoreListener(DistributoreListener l)    { listeners.add(l); }
    public void removeDistributoreListener(DistributoreListener l) { listeners.remove(l); }

    public Stato stato()  { return stato; }
    public int credito()  { return credito; }

    /** L'unico posto in cui si cambia stato. */
    void cambia(Stato nuovo) {
        stato = nuovo;
        for (DistributoreListener l : List.copyOf(listeners))
            l.statoCambiato(nuovo.nome());
        nuovo.entra(this);
    }

    void setCredito(int c) {
        credito = c;
        for (DistributoreListener l : List.copyOf(listeners))
            l.creditoCambiato(c);
    }

    void eroga(String prodotto) {
        for (DistributoreListener l : List.copyOf(listeners))
            l.prodottoErogato(prodotto);
    }

    void daiResto(int centesimi) {
        for (DistributoreListener l : List.copyOf(listeners))
            l.restoDato(centesimi);
    }

    void messaggio(String testo) {
        for (DistributoreListener l : List.copyOf(listeners))
            l.messaggio(testo);
    }

    public void esegui(Comando c) {
        stato.esegui(this, c);
    }

    public void stepNext(double dt) {
        stato.stepNext(this, dt);
    }
}
