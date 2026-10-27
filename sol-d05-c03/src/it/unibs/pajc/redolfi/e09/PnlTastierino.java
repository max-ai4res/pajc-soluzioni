package it.unibs.pajc.redolfi.e09;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

/**
 * Un tastierino come componente: dentro dieci cifre e "C", fuori un solo evento, un
 * ActionEvent con il tasto premuto come comando. Chi lo usa non vede i JButton.
 */
public class PnlTastierino extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String[] TASTI = { "7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0" };

    private final EventListenerList listeners = new EventListenerList();

    public PnlTastierino() {
        super(new GridLayout(0, 3, 4, 4));
        for (String t : TASTI) {
            JButton b = new JButton(t);
            b.addActionListener(e -> fireActionPerformed(t));   // listener interno
            add(b);
        }
    }

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    protected void fireActionPerformed(String tasto) {
        var e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, tasto);
        for (ActionListener l : listeners.getListeners(ActionListener.class))
            l.actionPerformed(e);
    }
}
