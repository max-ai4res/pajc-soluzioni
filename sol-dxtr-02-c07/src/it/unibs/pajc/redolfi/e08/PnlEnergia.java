package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JPanel;

/**
 * La seconda vista: il grafico dell'energia meccanica nel tempo. Ascolta il mondo e
 * campiona ogni decimo di secondo di TEMPO DEL MONDO; non ha un timer suo e nessuno la
 * chiama. Tiene solo la storia dei campioni.
 */
public class PnlEnergia extends JPanel implements MondoListener {

    private static final int CAMPIONI = 300;             // 30 secondi
    private final Deque<Double> storia = new ArrayDeque<>();
    private Mondo mondo;
    private double prossimo;

    public PnlEnergia(Mondo mondo) {
        setMondo(mondo);
        setBackground(new Color(0x0A1018));
        setPreferredSize(new java.awt.Dimension(100, 120));
    }

    /** Il controllore sostituisce il mondo: la vista si ri-registra e ricomincia la storia. */
    public Mondo mondoCorrente() {
        return mondo;
    }

    public void setMondo(Mondo nuovo) {
        if (mondo != null)
            mondo.removeMondoListener(this);
        mondo = nuovo;
        mondo.addMondoListener(this);
        storia.clear();
        prossimo = 0;
        repaint();
    }

    @Override
    public void avanzato(double tempo) {
        while (tempo >= prossimo) {                      // anche piu campioni se il passo e' stato lungo
            storia.addLast(mondo.energia());
            if (storia.size() > CAMPIONI)
                storia.removeFirst();
            prossimo += 0.1;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (storia.size() < 2)
            return;
        double max = storia.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (max <= 0)
            max = 1;
        double passoX = (double) getWidth() / (CAMPIONI - 1);
        Path2D.Double linea = new Path2D.Double();
        int i = CAMPIONI - storia.size();
        for (double e : storia) {
            double x = i * passoX, y = getHeight() - 8 - (getHeight() - 16) * e / max;
            if (linea.getCurrentPoint() == null) linea.moveTo(x, y); else linea.lineTo(x, y);
            i++;
        }
        g2.setColor(new Color(0xE0A020));
        g2.draw(linea);
        g2.setColor(Color.GRAY);
        g2.drawString(String.format("energia  max %.1f J", max), 8, 14);
    }
}
