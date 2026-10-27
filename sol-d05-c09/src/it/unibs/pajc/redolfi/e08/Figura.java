package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Una figura della Lavagna, in coordinate del disegno; spostata() ne fa una copia traslata. */
public record Figura(Forma tipo, Color colore, float spessore, List<Point2D> punti) {

    public Figura {
        punti = List.copyOf(punti);
    }

    public Figura spostata(double dx, double dy) {
        List<Point2D> nuovi = new ArrayList<>();
        for (Point2D p : punti)
            nuovi.add(new Point2D.Double(p.getX() + dx, p.getY() + dy));
        return new Figura(tipo, colore, spessore, nuovi);
    }

    public Shape forma() {
        Point2D a = punti.get(0), b = punti.get(punti.size() - 1);
        double x = Math.min(a.getX(), b.getX()), y = Math.min(a.getY(), b.getY());
        double w = Math.abs(b.getX() - a.getX()), h = Math.abs(b.getY() - a.getY());
        return switch (tipo) {
            case RETTANGOLO -> new Rectangle2D.Double(x, y, w, h);
            case ELLISSE -> new Ellipse2D.Double(x, y, w, h);
            case MANO_LIBERA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(a.getX(), a.getY());
                for (Point2D q : punti.subList(1, punti.size()))
                    p.lineTo(q.getX(), q.getY());
                yield p;
            }
        };
    }
}
