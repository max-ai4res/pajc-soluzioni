package it.unibs.pajc.redolfi.e06;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 7.6 - assi cartesiani con le tacche ogni 50 pixel.
 *
 * L'origine va al centro con translate; l'asse y verso l'alto si ottiene con scale(1, -1),
 * ma il testo andrebbe poi raddrizzato: qui si sceglie l'altra strada, si resta nel sistema
 * dei pixel traslato (y verso il basso) e si scrive -y quando si disegna "in su". Le tacche
 * vanno da 50 fino a meta' larghezza meno lo spazio della freccia, a passi di 50, in +x e
 * in -x: funzionano per qualunque dimensione del pannello.
 *
 * Errore tipico: un numero fisso di tacche, o le etichette dei valori negativi sull'asse y
 * con il segno sbagliato (in pixel la y cresce verso il basso).
 */
public class Assi extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int PASSO = 50, FRECCIA = 12;

    public Assi() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int mw = getWidth() / 2, mh = getHeight() / 2;
            g2.translate(mw, mh);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1.2f));
            int xMax = mw - 6, yMax = mh - 6;
            g2.drawLine(-xMax, 0, xMax, 0);
            g2.drawLine(0, yMax, 0, -yMax);
            g2.fillPolygon(new int[] { xMax, xMax - FRECCIA, xMax - FRECCIA }, new int[] { 0, -5, 5 }, 3);
            g2.fillPolygon(new int[] { 0, -5, 5 }, new int[] { -yMax, -yMax + FRECCIA, -yMax + FRECCIA }, 3);
            g2.drawString("x", xMax - 8, -10);
            g2.drawString("y", 10, -yMax + 10);

            FontMetrics fm = g2.getFontMetrics();
            for (int x = PASSO; x < xMax - FRECCIA; x += PASSO)
                for (int s : new int[] { -1, 1 }) {
                    g2.drawLine(s * x, -4, s * x, 4);
                    String t = Integer.toString(s * x);
                    g2.drawString(t, s * x - fm.stringWidth(t) / 2, 18);
                }
            for (int y = PASSO; y < yMax - FRECCIA; y += PASSO)
                for (int s : new int[] { -1, 1 }) {
                    g2.drawLine(-4, -s * y, 4, -s * y);          // "in su" e' -y in pixel
                    String t = Integer.toString(s * y);
                    g2.drawString(t, -8 - fm.stringWidth(t), -s * y + fm.getAscent() / 2 - 1);
                }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Assi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new Assi());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
