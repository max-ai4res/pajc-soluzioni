package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 8.6 - un cerchio che segue il mouse.
 *
 * Lo stato e' la posizione (null se il mouse e' fuori), la tinta e il raggio. Un solo
 * MouseAdapter, registrato per i pulsanti, il movimento e la rotellina: mouseMoved e
 * mouseDragged aggiornano la posizione, mousePressed la tinta (pressed, non clicked: un
 * click con il mouse in movimento non genera mouseClicked), mouseWheelMoved il raggio,
 * mouseExited azzera la posizione. Ogni metodo cambia lo stato e chiama aggiorna().
 *
 * Errore tipico: registrare l'adapter solo con addMouseListener, e il cerchio non si muove.
 */
public class CerchioSegue extends JPanel {

    private static final long serialVersionUID = 1L;

    private Point posizione;
    private int tinta;
    private int raggio = 30;
    private final JLabel lblStato;

    public CerchioSegue(JLabel lblStato) {
        this.lblStato = lblStato;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 360));
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { posizione = e.getPoint(); aggiorna(); }
            @Override public void mouseDragged(MouseEvent e) { posizione = e.getPoint(); aggiorna(); }
            @Override public void mouseExited(MouseEvent e) { posizione = null; aggiorna(); }
            @Override public void mousePressed(MouseEvent e) { tinta = (tinta + 1) % 12; aggiorna(); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                raggio = Math.max(5, Math.min(100, raggio - 5 * e.getWheelRotation()));
                aggiorna();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
        aggiorna();
    }

    private void aggiorna() {
        lblStato.setText(posizione == null ? "fuori  |  raggio " + raggio
                : "x = " + posizione.x + "   y = " + posizione.y + "  |  raggio " + raggio);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (posizione == null)
            return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.getHSBColor(tinta / 12f, 0.7f, 0.95f));
            g2.fill(new Ellipse2D.Double(posizione.x - raggio, posizione.y - raggio, 2 * raggio, 2 * raggio));
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Il cerchio che segue");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JLabel stato = new JLabel(" ");
            stato.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            f.add(new CerchioSegue(stato), BorderLayout.CENTER);
            f.add(stato, BorderLayout.SOUTH);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
