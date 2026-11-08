package it.unibs.pajc.redolfi.e03;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 4.3, il biliardo senza buche.
 *
 * Idea: il Mondo del capitolo con gravita' zero, quattro sponde con restituzione 0,9,
 * l'attrito del panno applicato nel moto a ogni sottopasso (v *= 1 - k dt) e la quiete
 * decisa li', sotto la soglia, senza superficie d'appoggio. tutteFerme() dice quando si
 * puo' tirare; la vista disegna la stecca durante il trascinamento e il rilascio diventa
 * colpisci(bianca, velocita).
 *
 * Errore tipico: mettere l'attrito del panno in contatto(): agirebbe solo negli urti e le
 * palline scorrerebbero per sempre.
 */
public class BiliardoApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mondo mondo = new Mondo(2.84, 1.42);                    // un tavolo vero, in metri
            double r = 0.03;
            Pallina bianca = mondo.lascia(new Vec2(0.7, 0.71), r, 0.95);
            for (int fila = 0; fila < 4; fila++)                     // il triangolo: 1+2+3+4 = 10, ne usiamo 7
                for (int k = 0; k <= fila && mondo.palline().size() < 8; k++)
                    mondo.lascia(new Vec2(1.9 + fila * 2 * r * 0.87, 0.71 + (k - fila / 2.0) * 2 * r * 1.02), r, 0.95);
            for (Pallina p : mondo.palline())
                p.setVelocita(Vec2.ZERO);                            // tutte ferme, pronte
            PnlBiliardo vista = new PnlBiliardo(mondo, bianca);
            JFrame f = new JFrame("Biliardo: trascina dalla bianca e rilascia");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(vista);
            f.setSize(900, 500);
            f.setVisible(true);
            long[] ultimo = { System.nanoTime() };
            new Timer(16, e -> {
                long adesso = System.nanoTime();
                mondo.stepNext((adesso - ultimo[0]) / 1e9);
                ultimo[0] = adesso;
                vista.repaint();
            }).start();
        });
    }
}
