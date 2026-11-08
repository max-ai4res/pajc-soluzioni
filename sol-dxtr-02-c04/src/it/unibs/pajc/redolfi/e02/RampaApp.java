package it.unibs.pajc.redolfi.e02;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 4.2, il piano inclinato.
 *
 * Idea: Rampa e' un segmento; il punto piu' vicino e' la proiezione del centro limitata
 * agli estremi, la normale va dal punto al centro, e il contatto e' lo stesso di pavimento
 * e ostacoli. La rampa e' d'appoggio se la normale punta abbastanza in alto. L'attrito di
 * rotolamento e' applicato per unita' di tempo finche' c'e' contatto.
 *
 * Errore tipico: frenare solo nell'urto. La pallina su una rampa tocca a ogni passo e il
 * rallentamento dipenderebbe dal passo; per unita' di tempo no (ProvaRampa).
 */
public class RampaApp {

    public static Mondo scenario() {
        Mondo mondo = new Mondo(8, 6);
        mondo.aggiungi(new Rampa(new Vec2(0.5, 4.5), new Vec2(5.0, 2.5)));
        mondo.aggiungi(new Rampa(new Vec2(7.5, 2.0), new Vec2(3.0, 0.8)));
        return mondo;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mondo mondo = scenario();
            PnlRampa vista = new PnlRampa(mondo);
            JFrame f = new JFrame("Il piano inclinato: clicca per lasciare una pallina");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(vista);
            f.setSize(640, 480);
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
