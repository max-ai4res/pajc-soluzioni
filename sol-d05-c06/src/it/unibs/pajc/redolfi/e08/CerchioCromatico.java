package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Esercizio 6.8 - il cerchio cromatico.
 *
 * n settori Arc2D.PIE di ampiezza 360/n gradi, ciascuno con la tinta i/n; un cerchio bianco
 * al centro lo trasforma in una corona. Gli angoli sono double: con 7 settori l'ampiezza e'
 * 51,43 gradi, e Arc2D la accetta cosi' com'e' (fillArc di Graphics vorrebbe interi, e con
 * gli arrotondamenti lascerebbe una fessura).
 *
 * Errore tipico: i / n con due interi, che vale sempre 0 e colora tutto di rosso.
 */
public class CerchioCromatico extends JPanel {

    private static final long serialVersionUID = 1L;

    private int n = 12;

    public CerchioCromatico() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 420));
    }

    public void setSettori(int n) {
        this.n = n;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double r = Math.min(getWidth(), getHeight()) / 2.0 - 10;
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            double ampiezza = 360.0 / n;
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.getHSBColor(i / (float) n, 1f, 1f));
                g2.fill(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, 90 - i * ampiezza,
                        -ampiezza, Arc2D.PIE));                    // in senso orario dalle dodici
            }
            g2.setColor(getBackground());
            double ri = r * 0.55;
            g2.fill(new Ellipse2D.Double(cx - ri, cy - ri, 2 * ri, 2 * ri));
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Cerchio cromatico");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            CerchioCromatico c = new CerchioCromatico();
            JSlider sld = new JSlider(3, 72, 12);
            sld.addChangeListener(e -> c.setSettori(sld.getValue()));
            f.add(sld, BorderLayout.NORTH);
            f.add(c, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
