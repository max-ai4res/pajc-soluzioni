package it.unibs.pajc.redolfi.e01;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/** La vista: un termometro con la misurata (colonna) e la desiderata (tacca). Si ridisegna su evento. */
public class PnlTermometro extends JPanel {

    private static final double MIN = 10, MAX = 30;
    private final Termostato termostato;

    public PnlTermometro(Termostato termostato) {
        this.termostato = termostato;
        setBackground(Color.WHITE);
        termostato.addPropertyChangeListener(e -> repaint());    // qualunque proprieta': ridisegna
    }

    private double y(double gradi) {
        double h = getHeight() - 60;
        return 20 + h * (1 - (gradi - MIN) / (MAX - MIN));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        double x = getWidth() / 2.0, w = 24;
        g2.setColor(new Color(0xE0E4EA));
        g2.fill(new RoundRectangle2D.Double(x - w / 2, y(MAX), w, y(MIN) - y(MAX), w, w));
        g2.setColor(termostato.riscaldamentoAcceso() ? new Color(0xC94F2E) : new Color(0x24549C));
        double yM = y(termostato.misurata());
        g2.fill(new RoundRectangle2D.Double(x - w / 2 + 4, yM, w - 8, y(MIN) - yM, w - 8, w - 8));
        g2.fill(new Ellipse2D.Double(x - w, y(MIN) - 4, 2 * w, 2 * w));
        g2.setColor(Color.DARK_GRAY);
        double yD = y(termostato.desiderata());
        g2.drawLine((int) (x - w), (int) yD, (int) (x + w), (int) yD);
        g2.drawString(String.format("%.1f °C", termostato.misurata()), (int) (x + w + 8), (int) yM + 5);
        g2.drawString(String.format("obiettivo %.1f", termostato.desiderata()), (int) (x + w + 8), (int) yD - 4);
    }
}
