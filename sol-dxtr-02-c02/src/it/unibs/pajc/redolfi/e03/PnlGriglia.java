package it.unibs.pajc.redolfi.e03;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

/** La vista: la griglia adattata al pannello; dal pixel alla cella per chi riceve il mouse. */
public class PnlGriglia extends JPanel {

    private final Griglia griglia;

    public PnlGriglia(Griglia griglia) {
        this.griglia = griglia;
        setBackground(Color.WHITE);
    }

    private double lato() {
        return Math.min((double) getWidth() / griglia.colonne(), (double) getHeight() / griglia.righe());
    }

    /** La cella sotto un pixel, o null se e' fuori dalla griglia. */
    public Point cellaDaPixel(Point p) {
        int c = (int) (p.x / lato()), r = (int) (p.y / lato());
        return r < griglia.righe() && c < griglia.colonne() ? new Point(r, c) : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double l = lato();
        for (int r = 0; r < griglia.righe(); r++)
            for (int c = 0; c < griglia.colonne(); c++) {
                int x = (int) (c * l), y = (int) (r * l), w = (int) ((c + 1) * l) - x, h = (int) ((r + 1) * l) - y;
                g.setColor(griglia.viva(r, c) ? new Color(0x24549C) : Color.WHITE);
                g.fillRect(x, y, w, h);
                g.setColor(new Color(0xE0E4EA));
                g.drawRect(x, y, w, h);
            }
    }
}
