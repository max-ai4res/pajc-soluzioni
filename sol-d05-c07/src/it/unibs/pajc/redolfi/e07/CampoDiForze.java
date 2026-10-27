package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Esercizio 7.7 - una forma ripetuta n volte ruotando attorno al centro.
 *
 * Il triangolo si costruisce una volta, nel sistema con l'origine al centro: base vicino
 * all'origine, vertice verso destra. La ripetizione la fa rotate(2*pi/n), che si accumula:
 * dopo i giri il sistema e' ruotato di i/n di giro. Con "alterna" i triangoli dispari sono
 * piu' corti: due forme costruite una volta sola.
 *
 * Errore tipico: calcolare a mano i vertici di ogni triangolo con la trigonometria.
 */
public class CampoDiForze extends JPanel {

    private static final long serialVersionUID = 1L;

    private int n = 12;
    private boolean alterna;

    public CampoDiForze() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 420));
    }

    public void setN(int n) { this.n = n; repaint(); }

    public void setAlterna(boolean alterna) { this.alterna = alterna; repaint(); }

    private static Path2D triangolo(double r, double lunghezza) {
        Path2D t = new Path2D.Double();
        t.moveTo(r * 0.12, -r * 0.07);
        t.lineTo(r * lunghezza, 0);
        t.lineTo(r * 0.12, r * 0.07);
        t.closePath();
        return t;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double r = Math.min(getWidth(), getHeight()) / 2.0 - 8;
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            Path2D lungo = triangolo(r, 1.0), corto = triangolo(r, 0.65);
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.getHSBColor(i / (float) n, 0.7f, 0.9f));
                g2.fill(alterna && i % 2 == 1 ? corto : lungo);
                g2.rotate(2 * Math.PI / n);
            }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Campo di forze");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            CampoDiForze c = new CampoDiForze();
            JSlider sld = new JSlider(3, 60, 12);
            JCheckBox chk = new JCheckBox("Alterna");
            sld.addChangeListener(e -> c.setN(sld.getValue()));
            chk.addActionListener(e -> c.setAlterna(chk.isSelected()));
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
            barra.add(sld);
            barra.add(chk);
            f.add(barra, BorderLayout.NORTH);
            f.add(c, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
