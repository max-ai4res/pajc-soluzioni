package it.unibs.pajc.redolfi.e02;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Un pannello che disegna le forme in metri con la trasformazione di PnlMondo (8 x 6 m). */
public class PnlSfondo extends JPanel {

    private static final int BORDO = 10;
    private static final double LARGHEZZA = 8, ALTEZZA = 6;
    private final List<Shape> forme;

    public PnlSfondo(List<Shape> forme) {
        this.forme = forme;
        setBackground(new Color(0x101820));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double s = Math.min((getWidth() - 2 * BORDO) / LARGHEZZA, (getHeight() - 2 * BORDO) / ALTEZZA);
        AffineTransform t = new AffineTransform();       // metri -> pixel, y in su
        t.translate(BORDO, getHeight() - BORDO);
        t.scale(s, -s);
        g2.setColor(new Color(0x2A3A4A));
        for (Shape f : forme)
            g2.fill(t.createTransformedShape(f));        // la forma trasformata, non il contesto: i bordi restano sottili
        g2.setColor(new Color(0x5A6A7A));
        for (Shape f : forme)
            g2.draw(t.createTransformedShape(f));
        g2.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Lo sfondo 'casa' dalla fabbrica di forme");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new PnlSfondo(FabbricaForme.daRisorsa("casa")));
            f.setSize(640, 480);
            f.setVisible(true);
        });
    }
}
