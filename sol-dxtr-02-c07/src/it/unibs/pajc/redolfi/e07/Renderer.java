package it.unibs.pajc.redolfi.e07;

import java.awt.Graphics2D;

/** La Strategy del disegno: una per tipo di entita'. */
public interface Renderer<E extends Entita> {
    void disegna(Graphics2D g2, E e, double scala);
}
