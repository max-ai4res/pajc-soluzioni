package it.unibs.pajc.redolfi.e09;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JPanel;

/**
 * Il pannello del poligono o della stella: ha il suo stato (n, stella) e i metodi per
 * cambiarlo, che chiedono un ridisegno. Non sa niente della finestra che lo usa.
 */
public class PnlPoligono extends JPanel {

    private static final long serialVersionUID = 1L;

    private int n = 5;
    private boolean stella;

    public PnlPoligono() {
        setBackground(Color.WHITE);
    }

    public void setLati(int n) {
        this.n = n;
        repaint();
    }

    public void setStella(boolean stella) {
        this.stella = stella;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cx = getWidth() / 2, cy = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2 - 24;
        Polygon p = stella ? stella(cx, cy, r, r * 2 / 5, n) : regolare(cx, cy, r, n);

        g.setColor(new Color(0xF4, 0xC5, 0xB4));
        g.fillPolygon(p);
        g.setColor(new Color(0xC9, 0x4F, 0x2E));
        g.drawPolygon(p);
        g.setColor(new Color(0x24, 0x54, 0x9C));
        for (int i = 0; i < p.npoints; i++) {
            int x = p.xpoints[i], y = p.ypoints[i];
            g.fillOval(x - 3, y - 3, 7, 7);
            // il numero un po' fuori dal vertice, sulla retta dal centro
            double d = Math.hypot(x - cx, y - cy);
            int tx = (int) Math.round(x + (x - cx) / d * 12), ty = (int) Math.round(y + (y - cy) / d * 12);
            String s = Integer.toString(i);
            g.drawString(s, tx - g.getFontMetrics().stringWidth(s) / 2, ty + 5);
        }
    }

    static Polygon regolare(int cx, int cy, int r, int n) {
        Polygon p = new Polygon();
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n - Math.PI / 2;
            p.addPoint((int) Math.round(cx + r * Math.cos(a)), (int) Math.round(cy + r * Math.sin(a)));
        }
        return p;
    }

    static Polygon stella(int cx, int cy, int rEst, int rInt, int n) {
        Polygon p = new Polygon();
        for (int i = 0; i < 2 * n; i++) {
            double a = Math.PI * i / n - Math.PI / 2;
            int r = i % 2 == 0 ? rEst : rInt;
            p.addPoint((int) Math.round(cx + r * Math.cos(a)), (int) Math.round(cy + r * Math.sin(a)));
        }
        return p;
    }
}
