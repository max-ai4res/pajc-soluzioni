package it.unibs.pajc.redolfi.e03;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Le due pile. Esporta due proprieta', "annullabile" e "ripetibile", per abilitare le voci del menu. */
public class Storia {

    private final List<Figura> figure;
    private final Deque<Comando> fatti = new ArrayDeque<>(), disfatti = new ArrayDeque<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public Storia(List<Figura> figure) {
        this.figure = figure;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        proprieta.addPropertyChangeListener(l);
    }

    public boolean annullabile() { return !fatti.isEmpty(); }
    public boolean ripetibile()  { return !disfatti.isEmpty(); }

    private void annuncia(boolean a, boolean r) {
        proprieta.firePropertyChange("annullabile", a, annullabile());
        proprieta.firePropertyChange("ripetibile", r, ripetibile());
    }

    public void esegui(Comando c) {
        boolean a = annullabile(), r = ripetibile();
        c.esegui(figure);
        fatti.push(c);
        disfatti.clear();                                // un futuro nuovo cancella quello vecchio
        annuncia(a, r);
    }

    public void annulla() {
        if (fatti.isEmpty())
            return;
        boolean a = annullabile(), r = ripetibile();
        Comando c = fatti.pop();
        c.annulla(figure);
        disfatti.push(c);
        annuncia(a, r);
    }

    public void ripeti() {
        if (disfatti.isEmpty())
            return;
        boolean a = annullabile(), r = ripetibile();
        Comando c = disfatti.pop();
        c.esegui(figure);
        fatti.push(c);
        annuncia(a, r);
    }
}
