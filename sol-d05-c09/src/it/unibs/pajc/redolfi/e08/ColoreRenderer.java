package it.unibs.pajc.redolfi.e08;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/** Ogni voce con il quadratino del suo colore accanto al nome. */
public class ColoreRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Colore c)
            setIcon(new IconaColore(c.colore(), 12));
        return this;
    }
}
