package it.unibs.pajc.redolfi.e09;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.JPanel;

/** Il pannello che disegna la griglia dal Model e traduce un click in (riga, colonna). */
public class PnlGriglia extends JPanel {

    private static final long serialVersionUID = 1L;
    private final TrisModel model;

    public PnlGriglia(TrisModel model, BiConsumer<Integer, Integer> onClick) {
        this.model = model;
        setPreferredSize(new Dimension(300, 300));
        setBackground(Color.WHITE);
        model.addPropertyChangeListener(e -> repaint());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!model.turnoMio()) return;            // il turno e' uno stato del Model
                int lato = Math.min(getWidth(), getHeight());
                int x0 = (getWidth() - lato) / 2, y0 = (getHeight() - lato) / 2;
                int c = (e.getX() - x0) * 3 / lato, r = (e.getY() - y0) * 3 / lato;
                if (r >= 0 && r < 3 && c >= 0 && c < 3) onClick.accept(r, c);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int lato = Math.min(getWidth(), getHeight()) - 20;
        int x0 = (getWidth() - lato) / 2, y0 = (getHeight() - lato) / 2, cella = lato / 3;
        g.setColor(model.turnoMio() ? new Color(0x2E, 0x7D, 0x8C) : Color.GRAY);
        g.setStroke(new BasicStroke(3f));
        for (int i = 1; i < 3; i++) {
            g.drawLine(x0 + i * cella, y0, x0 + i * cella, y0 + lato);
            g.drawLine(x0, y0 + i * cella, x0 + lato, y0 + i * cella);
        }
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                int x = x0 + c * cella + cella / 5, y = y0 + r * cella + cella / 5, d = cella * 3 / 5;
                char s = model.cella(r, c);
                if (s == 'X') {
                    g.setColor(new Color(0xC9, 0x4F, 0x2E));
                    g.drawLine(x, y, x + d, y + d);
                    g.drawLine(x + d, y, x, y + d);
                } else if (s == 'O') {
                    g.setColor(new Color(0x2E, 0x5C, 0xC9));
                    g.drawOval(x, y, d, d);
                }
            }
    }
}
