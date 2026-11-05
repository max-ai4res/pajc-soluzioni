package it.unibs.pajc.redolfi.e07;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;

/** L'istogramma dei voti: si disegna dallo stato che gli viene passato (risposte e conteggi). */
public class PnlIstogramma extends JPanel {
    private static final long serialVersionUID = 1L;
    private List<String> risposte = List.of();
    private int[] conteggi = new int[0];

    public PnlIstogramma() { setPreferredSize(new Dimension(420, 240)); setBackground(Color.WHITE); }

    public void mostra(List<String> risposte, int[] conteggi) {
        this.risposte = risposte;
        this.conteggi = conteggi;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (risposte.isEmpty()) { g.setColor(Color.GRAY); g.drawString("nessuna domanda aperta", 20, 30); return; }
        int n = risposte.size(), max = 1;
        for (int c : conteggi) max = Math.max(max, c);
        int w = getWidth(), h = getHeight(), colonna = w / n, base = h - 40;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int i = 0; i < n; i++) {
            int c = i < conteggi.length ? conteggi[i] : 0;
            int alt = (int) ((base - 30) * (double) c / max);
            int x = i * colonna + colonna / 5;
            g.setColor(new Color(0x2E, 0x7D, 0x8C));
            g.fillRect(x, base - alt, colonna * 3 / 5, alt);
            g.setColor(Color.DARK_GRAY);
            String eti = (i + 1) + ". " + risposte.get(i);
            g.drawString(eti.length() > 18 ? eti.substring(0, 17) + "..." : eti, x, base + 16);
            g.drawString(String.valueOf(c), x + colonna / 4, base - alt - 4);
        }
    }
}
