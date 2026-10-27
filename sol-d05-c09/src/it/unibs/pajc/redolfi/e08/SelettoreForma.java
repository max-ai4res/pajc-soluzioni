package it.unibs.pajc.redolfi.e08;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;

/**
 * Un componente nostro: tre pulsanti a levetta, uno per forma, e un evento solo, "la forma
 * e' cambiata". Chi lo usa non sa che dentro ci sono tre pulsanti: registra un
 * ActionListener e chiede getForma().
 */
public class SelettoreForma extends JPanel {

    private static final long serialVersionUID = 1L;

    private final EventListenerList listeners = new EventListenerList();
    private Forma forma = Forma.RETTANGOLO;

    public SelettoreForma() {
        super(new GridLayout(1, 0));
        ButtonGroup gruppo = new ButtonGroup();          // uno solo premuto alla volta
        for (Forma f : Forma.values()) {
            JToggleButton b = new JToggleButton(f.etichetta(), f == forma);
            b.addActionListener(e -> scegli(f));
            gruppo.add(b);
            add(b);
        }
    }

    public Forma getForma() {
        return forma;
    }

    private void scegli(Forma f) {
        if (f == forma)
            return;                                      // nessun cambiamento, nessun evento
        forma = f;
        fireActionPerformed();
    }

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    protected void fireActionPerformed() {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, forma.name());
        for (ActionListener l : listeners.getListeners(ActionListener.class))
            l.actionPerformed(e);
    }
}
