package it.unibs.pajc.redolfi.e06;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Problema 9.6 - la dama.
 *
 * Lo stato e' una matrice di Pedina (enum con VUOTA, BIANCA, NERA), il turno e la casella
 * selezionata; il pannello lo disegna e basta. La geometria della scacchiera (lato della
 * casella e margine) sta in un solo metodo, usato da paintComponent e da casella(Point),
 * che converte il pixel del click nella casella: la conversione non puo' divergere dal
 * disegno. Le bianche muovono verso l'alto (riga che diminuisce), le nere verso il basso;
 * una mossa e' un passo in diagonale in avanti su una casella vuota, una presa un salto di
 * due su una casella vuota con una pedina avversaria in mezzo.
 *
 * Errore tipico: rifare a mano il calcolo del lato nel listener, con un margine diverso.
 */
public class Dama extends JPanel {

    private static final long serialVersionUID = 1L;

    enum Pedina { VUOTA, BIANCA, NERA }

    private final Pedina[][] caselle = new Pedina[8][8];
    private Pedina turno = Pedina.BIANCA;
    private Point selezionata;                          // (colonna, riga), o null
    private final JLabel lblStato;

    public Dama(JLabel lblStato) {
        this.lblStato = lblStato;
        setBackground(new Color(0x30, 0x24, 0x1A));
        setPreferredSize(new Dimension(480, 480));
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                caselle[r][c] = !scura(r, c) ? Pedina.VUOTA : r < 3 ? Pedina.NERA : r > 4 ? Pedina.BIANCA : Pedina.VUOTA;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = casella(e.getPoint());
                if (p != null)
                    click(p.x, p.y);
            }
        });
        aggiornaStato();
    }

    static boolean scura(int r, int c) { return (r + c) % 2 == 1; }

    /** La geometria: lato della casella e angolo della scacchiera. */
    private int[] geometria() {
        int lato = Math.min(getWidth(), getHeight()) / 8;
        return new int[] { lato, (getWidth() - 8 * lato) / 2, (getHeight() - 8 * lato) / 2 };
    }

    private Point casella(Point pixel) {
        int[] g = geometria();
        int c = Math.floorDiv(pixel.x - g[1], g[0]), r = Math.floorDiv(pixel.y - g[2], g[0]);
        return r >= 0 && r < 8 && c >= 0 && c < 8 ? new Point(c, r) : null;
    }

    private void click(int c, int r) {
        if (caselle[r][c] == turno) {                    // si sceglie (o si cambia) la pedina
            selezionata = new Point(c, r);
        } else if (selezionata != null && caselle[r][c] == Pedina.VUOTA && scura(r, c)) {
            int dr = r - selezionata.y, dc = c - selezionata.x;
            int avanti = turno == Pedina.BIANCA ? -1 : 1;
            boolean mossa = dr == avanti && Math.abs(dc) == 1;
            boolean presa = false;
            if (dr == 2 * avanti && Math.abs(dc) == 2) {
                Pedina inMezzo = caselle[selezionata.y + dr / 2][selezionata.x + dc / 2];
                presa = inMezzo != Pedina.VUOTA && inMezzo != turno;
            }
            if (mossa || presa) {
                if (presa)
                    caselle[selezionata.y + dr / 2][selezionata.x + dc / 2] = Pedina.VUOTA;
                caselle[r][c] = turno;
                caselle[selezionata.y][selezionata.x] = Pedina.VUOTA;
                selezionata = null;
                turno = turno == Pedina.BIANCA ? Pedina.NERA : Pedina.BIANCA;
            }
        }
        aggiornaStato();
        repaint();
    }

    private void aggiornaStato() {
        lblStato.setText("Tocca al " + (turno == Pedina.BIANCA ? "bianco" : "nero")
                + (selezionata == null ? "" : ": scegli dove muovere"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int[] geo = geometria();
            int lato = geo[0];
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 8; c++) {
                    int x = geo[1] + c * lato, y = geo[2] + r * lato;
                    g2.setColor(scura(r, c) ? new Color(0x8B, 0x5E, 0x3C) : new Color(0xEE, 0xE4, 0xCF));
                    g2.fillRect(x, y, lato, lato);
                    if (caselle[r][c] != Pedina.VUOTA) {
                        Ellipse2D p = new Ellipse2D.Double(x + lato * 0.12, y + lato * 0.12, lato * 0.76, lato * 0.76);
                        g2.setColor(caselle[r][c] == Pedina.BIANCA ? new Color(0xF8, 0xF8, 0xF4) : new Color(0x22, 0x22, 0x22));
                        g2.fill(p);
                        boolean sel = selezionata != null && selezionata.x == c && selezionata.y == r;
                        g2.setColor(sel ? new Color(0xE0, 0x8A, 0x1E) : Color.GRAY);
                        g2.setStroke(new BasicStroke(sel ? 4 : 1.5f));
                        g2.draw(p);
                    }
                }
        } finally {
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Dama");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JLabel stato = new JLabel();
            stato.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            f.add(new Dama(stato), BorderLayout.CENTER);
            f.add(stato, BorderLayout.SOUTH);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
