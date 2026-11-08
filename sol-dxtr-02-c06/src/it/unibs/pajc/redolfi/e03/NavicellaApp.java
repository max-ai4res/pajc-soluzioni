package it.unibs.pajc.redolfi.e03;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 6.3, il pool di proiettili.
 *
 * Idea: 64 proiettili creati una volta; sparare = riaccenderne uno spento (un solo metodo
 * azzera posizione, velocita' ed eta'); se sono tutti in volo la navicella non spara. Il
 * pannello mostra in volo e liberi. ProvaProiettili verifica i limiti senza finestra.
 *
 * Errore tipico: riaccendere un proiettile senza azzerare l'eta': muore subito e la
 * navicella "spara a vuoto" in modo intermittente.
 */
public class NavicellaApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Navicella nav = new Navicella();
            Set<Integer> premuti = new HashSet<>();
            JPanel vista = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    double s = Math.min(getWidth() / Navicella.LARGHEZZA, getHeight() / Navicella.ALTEZZA);
                    g2.setColor(Color.WHITE);
                    for (Proiettile p : nav.pool().tutti())
                        if (p.vivo())
                            g2.fill(new Ellipse2D.Double(p.posizione().x() * s - 3, getHeight() - p.posizione().y() * s - 3, 6, 6));
                    Path2D.Double forma = new Path2D.Double();
                    forma.moveTo(0.6, 0);
                    forma.lineTo(-0.4, 0.35);
                    forma.lineTo(-0.2, 0);
                    forma.lineTo(-0.4, -0.35);
                    forma.closePath();
                    AffineTransform t = new AffineTransform();
                    t.translate(nav.posizione().x() * s, getHeight() - nav.posizione().y() * s);
                    t.scale(s, -s);
                    t.rotate(nav.angolo());
                    g2.setColor(new Color(0xE0A020));
                    g2.fill(t.createTransformedShape(forma));
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawString("in volo " + nav.pool().inVolo() + "   liberi " + nav.pool().liberi() + "   frecce: ruota, spazio: spara", 10, 20);
                }
            };
            vista.setBackground(new Color(0x101820));
            vista.setFocusable(true);
            vista.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    premuti.add(e.getKeyCode());
                    if (e.getKeyCode() == KeyEvent.VK_SPACE)
                        nav.spara();
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    premuti.remove(e.getKeyCode());
                }
            });
            JFrame f = new JFrame("Navicella");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(vista);
            f.setSize(800, 620);
            f.setVisible(true);
            vista.requestFocusInWindow();
            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                nav.ruota((premuti.contains(KeyEvent.VK_LEFT) ? 1 : 0) - (premuti.contains(KeyEvent.VK_RIGHT) ? 1 : 0));
                nav.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
                vista.repaint();
            }).start();
        });
    }
}
