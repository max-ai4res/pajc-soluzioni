package it.unibs.pajc.redolfi.e04;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Il disegnatore che applica un tema: il colore lo decide dal materiale, al momento del disegno. */
public class DisegnatoreTema implements Disegnatore {

    private final Tema tema;

    public DisegnatoreTema(Tema tema) {
        this.tema = tema;
    }

    @Override
    public Tema tema() {
        return tema;
    }

    @Override
    public void pallina(Graphics2D g2, Pallina p, double x, double y, double r) {
        Ellipse2D forma = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
        Color c = switch (p.materiale()) {
            case Gomma g -> p.ferma() ? tema.gommaFerma() : tema.gomma();
            case Piombo pb -> tema.piombo();
            case Bolla b -> tema.bolla();
        };
        if (p.materiale() instanceof Bolla) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(Math.max(1, tema.bordo())));
            g2.draw(forma);
        } else {
            g2.setColor(c);
            g2.fill(forma);
            if (tema.bordo() > 0) {
                g2.setColor(tema.pavimento());
                g2.setStroke(new BasicStroke(tema.bordo()));
                g2.draw(forma);
            }
        }
    }

    @Override
    public void scheggia(Graphics2D g2, Particella s, double x, double y, double r) {
        if (!tema.schegge())
            return;
        Color c = tema.gomma();
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (200 * s.vitaResidua())));
        g2.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
    }
}
