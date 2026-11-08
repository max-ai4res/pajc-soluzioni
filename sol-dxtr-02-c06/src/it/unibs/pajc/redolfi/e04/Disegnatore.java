package it.unibs.pajc.redolfi.e04;

import java.awt.Graphics2D;

/** La Strategy del disegno: come si disegnano una pallina e una scheggia, dato un tema. */
public interface Disegnatore {

    void pallina(Graphics2D g2, Pallina p, double x, double y, double r);

    void scheggia(Graphics2D g2, Particella s, double x, double y, double r);

    Tema tema();
}
