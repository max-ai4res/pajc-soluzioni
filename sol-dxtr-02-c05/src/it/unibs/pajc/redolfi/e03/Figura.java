package it.unibs.pajc.redolfi.e03;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/** Una figura dell'editor: tipo, posizione e misure in pixel (qui il modello e' lo schermo). */
public class Figura {

    public enum Tipo { RETTANGOLO, ELLISSE }

    private final Tipo tipo;
    private double x, y;
    private final double w, h;

    public Figura(Tipo tipo, double x, double y, double w, double h) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public double x() { return x; }
    public double y() { return y; }

    public void spostaIn(double nx, double ny) {
        x = nx;
        y = ny;
    }

    public Shape forma() {
        return tipo == Tipo.RETTANGOLO ? new Rectangle2D.Double(x, y, w, h) : new Ellipse2D.Double(x, y, w, h);
    }
}
