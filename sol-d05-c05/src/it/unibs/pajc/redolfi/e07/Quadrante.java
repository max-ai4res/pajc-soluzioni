package it.unibs.pajc.redolfi.e07;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 5.7 - il quadrante di un orologio con le primitive di Graphics.
 *
 * Tutte le posizioni vengono da punto(angolo, raggio): l'angolo e' in "minuti" (0..60),
 * misurato dalle ore dodici in senso orario. Sullo schermo, con l'asse y verso il basso,
 * x = cx + r sin(a) e y = cy - r cos(a) danno proprio quel verso: a 0 il punto e' in alto,
 * a 15 a destra. Le lancette sono segmenti dal centro; quella delle ore avanza anche con i
 * minuti (10:10 non e' sulle 10 esatte).
 *
 * Errore tipico: usare cos per x e sin per y, che fa partire il quadrante dalle ore tre e
 * lo fa girare in senso antiorario.
 */
public class Quadrante extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int ORE = 10, MINUTI = 10, SECONDI = 30;

    private int cx, cy;

    public Quadrante() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    /** Il punto a distanza r dal centro, all'angolo dato in sessantesimi di giro. */
    private int[] punto(double sessantesimi, double r) {
        double a = 2 * Math.PI * sessantesimi / 60;
        return new int[] { (int) Math.round(cx + r * Math.sin(a)),
                           (int) Math.round(cy - r * Math.cos(a)) };
    }

    private void segmento(Graphics g, double angolo, double r1, double r2) {
        int[] a = punto(angolo, r1), b = punto(angolo, r2);
        g.drawLine(a[0], a[1], b[0], b[1]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        cx = getWidth() / 2;
        cy = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2 - 10;

        g.setColor(Color.DARK_GRAY);
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        for (int i = 0; i < 60; i++) {
            boolean ora = i % 5 == 0;
            segmento(g, i, r * (ora ? 0.86 : 0.93), r);
            if (ora)
                segmento(g, i + 0.08, r * 0.86, r);             // la tacca delle ore: piu' spessa
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, r / 9)));
        FontMetrics fm = g.getFontMetrics();
        for (int h = 1; h <= 12; h++) {
            int[] p = punto(h * 5, r * 0.72);
            String s = Integer.toString(h);
            g.drawString(s, p[0] - fm.stringWidth(s) / 2, p[1] + (fm.getAscent() - fm.getDescent()) / 2);
        }

        g.setColor(Color.BLACK);
        double oreAngolo = (ORE % 12 + MINUTI / 60.0) * 5;
        segmento(g, oreAngolo, 0, r * 0.5);
        segmento(g, oreAngolo + 0.1, 0, r * 0.5);
        segmento(g, MINUTI + SECONDI / 60.0, 0, r * 0.78);
        g.setColor(new Color(0xC9, 0x4F, 0x2E));
        segmento(g, SECONDI, -r * 0.12, r * 0.85);
        g.fillOval(cx - 4, cy - 4, 8, 8);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Quadrante");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Quadrante());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
