package it.unibs.pajc.redolfi.e09;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 7.9 - il grafico di due funzioni, con legenda, zoom e spostamento.
 *
 * Le funzioni sono record (nome, funzione, colore). La scala verticale si calcola sui
 * campioni di TUTTE le funzioni, cosi' che entrambe stiano nel pannello. Zoom e
 * spostamento cambiano solo l'intervallo delle x, attorno al suo centro: zoom(0.5) lo
 * dimezza, sposta(0.25) lo trasla di un quarto della sua ampiezza. Le conversioni px, py e
 * screenToWorldX sono quelle della sezione 7.1; la curva si interrompe sui campioni non finiti.
 *
 * Errore tipico: calcolare yMin e yMax per ciascuna funzione separatamente, con due scale
 * diverse sullo stesso asse.
 */
public class PlotDoppio extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int M = 40;                              // margine per le etichette

    record Funzione(String nome, DoubleUnaryOperator f, Color colore) { }

    private final List<Funzione> funzioni;
    private double xMin = -10, xMax = 10, yMin, yMax;

    public PlotDoppio(List<Funzione> funzioni) {
        this.funzioni = List.copyOf(funzioni);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(640, 400));
    }

    public void zoom(double fattore) {
        double c = (xMin + xMax) / 2, meta = (xMax - xMin) / 2 * fattore;
        xMin = c - meta;
        xMax = c + meta;
        repaint();
    }

    public void sposta(double frazione) {
        double d = (xMax - xMin) * frazione;
        xMin += d;
        xMax += d;
        repaint();
    }

    private int w() { return getWidth() - 2 * M; }
    private int h() { return getHeight() - 2 * M; }
    private double worldToScreenX(double x) { return M + (x - xMin) / (xMax - xMin) * w(); }
    private double worldToScreenY(double y) { return M + (yMax - y) / (yMax - yMin) * h(); }
    private double screenToWorldX(int i) { return xMin + (double) i / w() * (xMax - xMin); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (w() <= 0 || h() <= 0)
            return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double[][] ys = new double[funzioni.size()][w() + 1];
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < funzioni.size(); k++)
                for (int i = 0; i <= w(); i++) {
                    double y = funzioni.get(k).f().applyAsDouble(screenToWorldX(i));
                    ys[k][i] = y;
                    if (Double.isFinite(y)) {
                        min = Math.min(min, y);
                        max = Math.max(max, y);
                    }
                }
            if (min > max) { min = -1; max = 1; }
            if (max - min < 1e-9) { min -= 1; max += 1; }
            double margine = (max - min) * 0.05;
            yMin = min - margine;
            yMax = max + margine;

            g2.setColor(Color.GRAY);
            g2.drawRect(M, M, w(), h());
            if (yMin < 0 && yMax > 0)
                g2.draw(new Line2D.Double(M, worldToScreenY(0), M + w(), worldToScreenY(0)));
            if (xMin < 0 && xMax > 0)
                g2.draw(new Line2D.Double(worldToScreenX(0), M, worldToScreenX(0), M + h()));
            g2.drawString(String.format("%.2f", xMin), M, M + h() + 16);
            String sx = String.format("%.2f", xMax);
            g2.drawString(sx, M + w() - g2.getFontMetrics().stringWidth(sx), M + h() + 16);

            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int k = 0; k < funzioni.size(); k++) {
                Path2D curva = new Path2D.Double();
                boolean giu = false;
                for (int i = 0; i <= w(); i++) {
                    if (!Double.isFinite(ys[k][i])) { giu = false; continue; }
                    if (giu) curva.lineTo(M + i, worldToScreenY(ys[k][i]));
                    else curva.moveTo(M + i, worldToScreenY(ys[k][i]));
                    giu = true;
                }
                g2.setColor(funzioni.get(k).colore());
                g2.draw(curva);
            }

            FontMetrics fm = g2.getFontMetrics();                 // la legenda
            int y = M + 18;
            for (Funzione f : funzioni) {
                int x = M + w() - 10 - fm.stringWidth(f.nome());
                g2.setColor(f.colore());
                g2.draw(new Line2D.Double(x - 30, y - 4, x - 8, y - 4));
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(f.nome(), x, y);
                y += fm.getHeight() + 2;
            }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Due funzioni");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PlotDoppio p = new PlotDoppio(List.of(
                    new Funzione("sin(x)", Math::sin, new Color(0x24, 0x54, 0x9C)),
                    new Funzione("x / 5", x -> x / 5, new Color(0xC9, 0x4F, 0x2E))));
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
            String[] nomi = { "Zoom +", "Zoom -", "<-", "->" };
            Runnable[] azioni = { () -> p.zoom(0.5), () -> p.zoom(2), () -> p.sposta(-0.25), () -> p.sposta(0.25) };
            for (int i = 0; i < nomi.length; i++) {
                Runnable a = azioni[i];
                JButton b = new JButton(nomi[i]);
                b.addActionListener(e -> a.run());
                barra.add(b);
            }
            f.add(barra, BorderLayout.NORTH);
            f.add(p, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
