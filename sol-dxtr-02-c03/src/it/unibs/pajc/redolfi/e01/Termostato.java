package it.unibs.pajc.redolfi.e01;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Il modello: tre proprieta' annunciate con PropertyChangeSupport. "misurata" cambia col
 * tempo, "desiderata" ha un setter; "riscaldamentoAcceso" e' derivata dalle altre due con
 * un'isteresi di mezzo grado, e va annunciata in ogni punto in cui puo' cambiare.
 */
public class Termostato {

    private static final double ISTERESI = 0.5;             // gradi: si riaccende solo sotto desiderata - 0,5
    private double misurata = 18.0, desiderata = 20.0;
    private double tempo;                                   // secondi dall'avvio
    private boolean acceso = true;
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        proprieta.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        proprieta.removePropertyChangeListener(l);
    }

    public double misurata()   { return misurata; }
    public double desiderata() { return desiderata; }
    public double tempo()      { return tempo; }

    public boolean riscaldamentoAcceso() {
        return acceso;
    }

    /** La regola con isteresi: si spegne raggiunta la desiderata, si riaccende mezzo grado sotto. */
    private void decidi() {
        if (acceso && misurata >= desiderata)
            acceso = false;
        else if (!acceso && misurata < desiderata - ISTERESI)
            acceso = true;
    }

    public void setDesiderata(double d) {
        boolean accesoPrima = riscaldamentoAcceso();
        double vecchia = desiderata;
        desiderata = d;
        decidi();
        proprieta.firePropertyChange("desiderata", vecchia, d);
        proprieta.firePropertyChange("riscaldamentoAcceso", accesoPrima, riscaldamentoAcceso());
    }

    /** dt secondi: la misurata sale di 1 grado/s col riscaldamento acceso, scende di 0,5 grado/s spento. */
    public void stepNext(double dt) {
        boolean accesoPrima = riscaldamentoAcceso();
        double vecchia = misurata;
        tempo += dt;
        misurata = acceso ? misurata + 1.0 * dt : Math.max(10, misurata - 0.5 * dt);
        decidi();
        proprieta.firePropertyChange("misurata", vecchia, misurata);
        proprieta.firePropertyChange("riscaldamentoAcceso", accesoPrima, riscaldamentoAcceso());
    }
}
