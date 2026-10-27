package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Il gioco della vita: una griglia toroidale, una regola come lambda, un javax.swing.Timer.
 *
 * stepNext() calcola la generazione nuova in una matrice NUOVA, leggendo solo la vecchia, e
 * poi la sostituisce: aggiornare sul posto farebbe contare vicini gia' cambiati. I vicini
 * si contano con gli indici modulo le dimensioni (il "+ righe" evita il resto negativo di
 * -1 % righe in Java). Il click converte il pixel nella cella con la stessa geometria di
 * paintComponent (lato, x0, y0).
 */
public class PnlVita extends JPanel {

    private static final long serialVersionUID = 1L;

    /** La regola: lo stato nuovo di una cella dato il suo stato e il numero di vicini vivi. */
    @FunctionalInterface
    public interface Regola {
        boolean viva(boolean viva, int vicini);
    }

    public static final Regola CONWAY = (viva, n) -> n == 3 || (viva && n == 2);

    private final int righe, colonne;
    private boolean[][] celle;
    private Regola regola = CONWAY;
    private final Timer timer = new Timer(120, e -> stepNext());

    public PnlVita(int righe, int colonne) {
        this.righe = righe;
        this.colonne = colonne;
        celle = new boolean[righe][colonne];
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int lato = lato();
                int c = (e.getX() - x0(lato)) / lato, r = (e.getY() - y0(lato)) / lato;
                if (e.getX() >= x0(lato) && e.getY() >= y0(lato) && r < righe && c < colonne) {
                    celle[r][c] = !celle[r][c];
                    repaint();
                }
            }
        });
    }

    public void setRegola(Regola regola) { this.regola = regola; }
    public void setIntervallo(int ms) { timer.setDelay(ms); }
    public void avviaFerma() { if (timer.isRunning()) timer.stop(); else timer.start(); }
    public boolean inCorso() { return timer.isRunning(); }

    public void svuota() {
        celle = new boolean[righe][colonne];
        repaint();
    }

    public void aCaso() {
        Random r = new Random();
        for (boolean[] riga : celle)
            for (int c = 0; c < colonne; c++)
                riga[c] = r.nextInt(4) == 0;
        repaint();
    }

    private int vicini(int r, int c) {
        int n = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++)
                if ((dr != 0 || dc != 0) && celle[(r + dr + righe) % righe][(c + dc + colonne) % colonne])
                    n++;
        return n;
    }

    private void stepNext() {
        boolean[][] nuove = new boolean[righe][colonne];
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++)
                nuove[r][c] = regola.viva(celle[r][c], vicini(r, c));
        celle = nuove;
        repaint();
    }

    private int lato() { return Math.max(1, Math.min(getWidth() / colonne, getHeight() / righe)); }
    private int x0(int lato) { return (getWidth() - lato * colonne) / 2; }
    private int y0(int lato) { return (getHeight() - lato * righe) / 2; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int lato = lato(), x0 = x0(lato), y0 = y0(lato);
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++) {
                g.setColor(celle[r][c] ? new Color(0x24, 0x54, 0x9C) : new Color(0xF2, 0xF4, 0xF7));
                g.fillRect(x0 + c * lato, y0 + r * lato, lato - 1, lato - 1);
            }
    }
}
