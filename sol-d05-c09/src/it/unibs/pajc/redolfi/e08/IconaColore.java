package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/** Un quadratino pieno di un colore: un'icona e' un valore, e diventa un record. */
public record IconaColore(Color colore, int lato) implements Icon {

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(colore);
        g.fillRect(x, y, lato, lato);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, lato - 1, lato - 1);
    }

    @Override
    public int getIconWidth() { return lato; }

    @Override
    public int getIconHeight() { return lato; }
}
