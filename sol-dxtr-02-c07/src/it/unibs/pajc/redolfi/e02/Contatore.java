package it.unibs.pajc.redolfi.e02;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/** Si incrementa da solo ogni secondo di tempo ricevuto; annuncia in due modi (lista a mano e proprieta'). */
public class Contatore {

    private int valore;
    private double accumulato;
    private final List<ContatoreListener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public void addContatoreListener(ContatoreListener l)           { listeners.add(l); }
    public void addPropertyChangeListener(PropertyChangeListener l) { proprieta.addPropertyChangeListener(l); }

    public int valore() {
        return valore;
    }

    public void stepNext(double dt) {
        accumulato += dt;
        while (accumulato >= 1.0) {
            accumulato -= 1.0;
            cambia(valore + 1);
        }
    }

    public void azzera() {
        cambia(0);
    }

    /** Si annuncia solo quando il valore cambia davvero. */
    private void cambia(int nuovo) {
        if (nuovo == valore)
            return;
        int vecchio = valore;
        valore = nuovo;
        for (ContatoreListener l : List.copyOf(listeners))
            l.valoreCambiato(nuovo);
        proprieta.firePropertyChange("valore", vecchio, nuovo);
    }
}
