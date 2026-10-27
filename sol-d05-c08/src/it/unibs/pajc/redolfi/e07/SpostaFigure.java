package it.unibs.pajc.redolfi.e07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 8.7 - spostare le figure trascinandole.
 *
 * Le figure sono record (forma, colore) in una lista: l'ultima e' disegnata per ultima, ed
 * e' quindi la piu' in alto. La pressione cerca dall'ultima la prima che contiene il punto,
 * la porta in fondo alla lista (primo piano) e ricorda il punto di presa. Il trascinamento
 * sposta la forma della differenza fra il punto nuovo e quello precedente, con una
 * traslazione applicata da createTransformedShape: la forma non "salta" sotto il mouse.
 *
 * Errore tipico: cercare dalla prima figura, che afferra quella sotto invece di quella
 * visibile.
 */
public class SpostaFigure extends JPanel {

    private static final long serialVersionUID = 1L;

    record Figura(Shape forma, Color colore) {
        Figura spostata(double dx, double dy) {
            return new Figura(AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(forma), colore);
        }
    }

    private final List<Figura> figure = new ArrayList<>();
    private int afferrata = -1;                     // indice nella lista, -1 nessuna
    private Point ultimo;

    public SpostaFigure() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 420));
        Random r = new Random(3);
        for (int i = 0; i < 10; i++) {
            double x = 20 + r.nextInt(460), y = 20 + r.nextInt(300), w = 50 + r.nextInt(80), h = 40 + r.nextInt(60);
            Shape s = i % 2 == 0 ? new Rectangle2D.Double(x, y, w, h) : new Ellipse2D.Double(x, y, w, h);
            figure.add(new Figura(s, Color.getHSBColor(r.nextFloat(), 0.55f, 0.95f)));
        }
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                afferrata = -1;
                for (int i = figure.size() - 1; i >= 0; i--)
                    if (figure.get(i).forma().contains(e.getPoint())) {
                        figure.add(figure.remove(i));      // in primo piano
                        afferrata = figure.size() - 1;
                        ultimo = e.getPoint();
                        break;
                    }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (afferrata < 0)
                    return;
                Figura f = figure.get(afferrata);
                figure.set(afferrata, f.spostata(e.getX() - ultimo.x, e.getY() - ultimo.y));
                ultimo = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                afferrata = -1;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < figure.size(); i++) {
                Figura f = figure.get(i);
                g2.setColor(f.colore());
                g2.fill(f.forma());
                g2.setColor(i == afferrata ? Color.BLACK : f.colore().darker());
                g2.setStroke(new BasicStroke(i == afferrata ? 3 : 1));
                g2.draw(f.forma());
            }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Sposta le figure");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new SpostaFigure());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
