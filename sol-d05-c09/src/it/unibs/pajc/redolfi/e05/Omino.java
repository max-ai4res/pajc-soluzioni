package it.unibs.pajc.redolfi.e05;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Problema 9.5 - un omino con le articolazioni.
 *
 * Il sistema dell'omino ha l'origine nel bacino e misura 100 unita' dalla testa ai piedi;
 * una scala lo adatta al lato minore. Un segmento e' un rettangolo arrotondato che parte
 * dall'origine del suo sistema e scende lungo y. segmento() disegna il segmento su una
 * copia del contesto ricevuto, ruotata dell'angolo dell'articolazione, e restituisce una
 * seconda copia gia' traslata in fondo al segmento: il sistema del segmento successivo.
 * Cosi' l'avambraccio parte dal gomito, che sta in fondo al braccio, e ruotando la spalla
 * si muove con lui.
 *
 * Errore tipico: calcolare la posizione del gomito con la trigonometria e ruotare
 * l'avambraccio dal sistema del busto.
 */
public class Omino extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color PELLE = new Color(0x24, 0x54, 0x9C);

    private double spalla = 40, gomito = 30, anca = 15, ginocchio = 20;    // gradi

    public Omino() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 420));
    }

    public void setAngoli(double spalla, double gomito, double anca, double ginocchio) {
        this.spalla = spalla;
        this.gomito = gomito;
        this.anca = anca;
        this.ginocchio = ginocchio;
        repaint();
    }

    /** Disegna un segmento lungo l e restituisce il contesto del suo estremo (da chiudere). */
    private static Graphics2D segmento(Graphics2D g, double gradi, double l) {
        Graphics2D s = (Graphics2D) g.create();
        s.rotate(Math.toRadians(gradi));
        s.fill(new RoundRectangle2D.Double(-3, -3, 6, l + 6, 6, 6));
        s.translate(0, l);                         // in fondo: il sistema del prossimo segmento
        return s;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scala = Math.min(getWidth(), getHeight()) / 110.0;
            g2.translate(getWidth() / 2.0, getHeight() / 2.0 + 5 * scala);
            g2.scale(scala, scala);
            g2.setColor(PELLE);

            // busto e testa, dal bacino verso l'alto
            g2.fill(new RoundRectangle2D.Double(-4, -32, 8, 34, 8, 8));
            g2.fill(new Ellipse2D.Double(-8, -50, 16, 16));

            // braccio sinistro fermo, braccio destro con spalla e gomito
            for (int lato : new int[] { -1, 1 }) {
                Graphics2D sp = (Graphics2D) g2.create();
                sp.translate(0, -28);                                 // la spalla
                // con y verso il basso un angolo positivo porta l'arto verso sinistra
                double a = lato == 1 ? -spalla : 20;          // destro: verso destra
                double b = lato == 1 ? -gomito : 10;
                Graphics2D gomitoG = segmento(sp, a, 18);
                Graphics2D mano = segmento(gomitoG, b, 16);
                mano.dispose();
                gomitoG.dispose();
                sp.dispose();
            }
            // gamba destra ferma, gamba sinistra con anca e ginocchio
            for (int lato : new int[] { -1, 1 }) {
                Graphics2D bacino = (Graphics2D) g2.create();
                bacino.translate(lato * 3, 0);
                double a = lato == -1 ? anca : -8;            // sinistra: verso sinistra
                double b = lato == -1 ? -ginocchio : 4;
                Graphics2D ginocchioG = segmento(bacino, a, 22);
                Graphics2D piede = segmento(ginocchioG, b, 22);
                piede.dispose();
                ginocchioG.dispose();
                bacino.dispose();
            }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Omino");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Omino o = new Omino();
            String[] nomi = { "Spalla", "Gomito", "Anca", "Ginocchio" };
            int[][] limiti = { { -90, 180, 40 }, { 0, 150, 30 }, { -90, 90, 15 }, { 0, 150, 20 } };
            JSlider[] sld = new JSlider[4];
            JPanel pnl = new JPanel(new GridLayout(0, 2, 6, 2));
            pnl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            for (int i = 0; i < 4; i++) {
                sld[i] = new JSlider(limiti[i][0], limiti[i][1], limiti[i][2]);
                sld[i].addChangeListener(e -> o.setAngoli(sld[0].getValue(), sld[1].getValue(),
                        sld[2].getValue(), sld[3].getValue()));
                pnl.add(new JLabel(nomi[i]));
                pnl.add(sld[i]);
            }
            f.add(pnl, BorderLayout.NORTH);
            f.add(o, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
