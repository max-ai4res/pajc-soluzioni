package it.unibs.pajc.redolfi.e06;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/** Tre forme tracciate con un BasicStroke tratteggiato i cui parametri arrivano da fuori. */
public class PnlTratto extends JPanel {

    private static final long serialVersionUID = 1L;

    private float spessore = 4, trattino = 12, fase = 0;
    private boolean arrotondato;

    public PnlTratto() {
        setBackground(Color.WHITE);
    }

    public void setTratto(float spessore, float trattino, float fase, boolean arrotondato) {
        this.spessore = spessore;
        this.trattino = trattino;
        this.fase = fase;
        this.arrotondato = arrotondato;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int estremi = arrotondato ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT;
            int giunzioni = arrotondato ? BasicStroke.JOIN_ROUND : BasicStroke.JOIN_MITER;
            float[] trattini = { trattino, trattino / 2 + spessore };   // il vuoto tiene conto dello spessore
            g2.setStroke(new BasicStroke(spessore, estremi, giunzioni, 10, trattini, fase));
            g2.setColor(new Color(0x24, 0x54, 0x9C));

            double w = getWidth() / 3.0, h = getHeight();
            g2.draw(new RoundRectangle2D.Double(20, 20, w - 40, h - 40, 30, 30));
            g2.draw(new Ellipse2D.Double(w + 20, 20, w - 40, h - 40));
            Path2D zig = new Path2D.Double();
            zig.moveTo(2 * w + 20, h - 20);
            for (int i = 1; i <= 6; i++)
                zig.lineTo(2 * w + 20 + i * (w - 40) / 6, i % 2 == 1 ? 20 : h - 20);
            g2.draw(zig);
        } finally {
            g2.dispose();
        }
    }
}
