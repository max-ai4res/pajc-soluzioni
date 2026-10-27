package it.unibs.pajc.redolfi.e02;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Problema 9.2 - cerchio e assi, come in una prova scritta: solo Graphics, niente di piu'.
 *
 * Il centro e' (w/2, h/2), il raggio meta' del lato minore; fillOval vuole l'angolo del
 * quadrato contenitore, quindi (cx - r, cy - r) e lato 2r. Il colore ha l'alfa (100 su
 * 255) perche' gli assi, disegnati prima, si vedano attraverso. Le tacche partono dal
 * centro a passi di 50, in entrambi i versi, finche' restano nel pannello.
 */
public class CerchioEAssi extends JPanel {

    private static final long serialVersionUID = 1L;

    public CerchioEAssi() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
        g.setColor(Color.BLACK);
        g.drawLine(0, cy, w, cy);
        g.drawLine(cx, 0, cx, h);
        for (int d = 50; cx + d < w || cy + d < h; d += 50) {
            g.drawLine(cx + d, cy - 4, cx + d, cy + 4);
            g.drawLine(cx - d, cy - 4, cx - d, cy + 4);
            g.drawLine(cx - 4, cy + d, cx + 4, cy + d);
            g.drawLine(cx - 4, cy - d, cx + 4, cy - d);
        }
        int r = Math.min(w, h) / 2;
        g.setColor(new Color(201, 79, 46, 100));
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Cerchio e assi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new CerchioEAssi());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
