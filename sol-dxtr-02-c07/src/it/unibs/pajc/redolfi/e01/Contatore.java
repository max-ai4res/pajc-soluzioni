package it.unibs.pajc.redolfi.e01;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** La terza versione, quella giusta: il modello tiene il numero e annuncia; non importa Swing. */
public class Contatore {

    private int valore;
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        proprieta.addPropertyChangeListener(l);
    }

    public int valore() {
        return valore;
    }

    public void incrementa() {
        System.out.println("  3. Contatore.incrementa: " + valore + " -> " + (valore + 1));
        int vecchio = valore++;
        System.out.println("  4. Contatore: firePropertyChange(\"valore\", " + vecchio + ", " + valore + ")");
        proprieta.firePropertyChange("valore", vecchio, valore);
    }
}
