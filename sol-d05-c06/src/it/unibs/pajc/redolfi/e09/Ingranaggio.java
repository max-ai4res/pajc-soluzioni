package it.unibs.pajc.redolfi.e09;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 6.9 - l'ingranaggio.
 *
 * Ogni dente occupa un angolo di 2*pi/n, diviso in quattro quarti: salita (dal raggio
 * interno all'esterno), cima (sul raggio esterno), discesa, fondo (sul raggio interno).
 * Quattro vertici per dente, un unico Path2D chiuso. Al profilo si sottrae il foro con
 * Area. La sfumatura radiale ha la luce spostata in alto a sinistra.
 *
 * Errore tipico: chiudere il percorso a ogni dente (closePath dentro il ciclo), che produce
 * n forme separate invece di un profilo.
 */
public class Ingranaggio extends JPanel {

    private static final long serialVersionUID = 1L;

    private int denti = 12;

    public Ingranaggio() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 420));
    }

    public void setDenti(int denti) {
        this.denti = denti;
        repaint();
    }

    static Area ingranaggio(double cx, double cy, double rEst, double rInt, double rForo, int n) {
        Path2D p = new Path2D.Double();
        double passo = 2 * Math.PI / n;
        for (int i = 0; i < n; i++) {
            double a = i * passo - Math.PI / 2;
            double[] angoli = { a, a + passo * 0.25, a + passo * 0.5, a + passo * 0.75 };
            double[] raggi = { rInt, rEst, rEst, rInt };
            for (int k = 0; k < 4; k++) {
                double x = cx + raggi[k] * Math.cos(angoli[k]), y = cy + raggi[k] * Math.sin(angoli[k]);
                if (i == 0 && k == 0)
                    p.moveTo(x, y);
                else
                    p.lineTo(x, y);
            }
        }
        p.closePath();
        Area area = new Area(p);
        area.subtract(new Area(new Ellipse2D.Double(cx - rForo, cy - rForo, 2 * rForo, 2 * rForo)));
        return area;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double r = Math.min(getWidth(), getHeight()) / 2.0 - 12;
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            Area a = ingranaggio(cx, cy, r, r * 0.8, r * 0.2, denti);
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(cx - r / 3, cy - r / 3), (float) (1.4 * r),
                    new float[] { 0f, 1f }, new Color[] { new Color(0xE8, 0xEC, 0xF2), new Color(0x5A, 0x67, 0x7A) }));
            g2.fill(a);
            g2.setColor(new Color(0x2A, 0x33, 0x40));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(a);
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Ingranaggio");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Ingranaggio ing = new Ingranaggio();
            JSpinner spn = new JSpinner(new SpinnerNumberModel(12, 6, 40, 1));
            spn.addChangeListener(e -> ing.setDenti((Integer) spn.getValue()));
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
            barra.add(new JLabel("Denti:"));
            barra.add(spn);
            f.add(barra, BorderLayout.NORTH);
            f.add(ing, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
