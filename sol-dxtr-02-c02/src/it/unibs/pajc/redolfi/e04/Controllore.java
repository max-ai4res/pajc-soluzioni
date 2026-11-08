package it.unibs.pajc.redolfi.e04;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

/** Il controllore con due viste a due ritmi: il pannello a ogni passo, la console una volta al secondo. */
public class Controllore {

    private final Mondo mondo;
    private final PnlMondo vista;
    private final VistaTestuale testuale;
    private final Timer timer = new Timer(16, e -> stepNext());
    private long ultimo;
    private double daUltimaRiga;                         // secondi dall'ultima riga a console

    public Controllore(Mondo mondo, PnlMondo vista, VistaTestuale testuale) {
        this.mondo = mondo;
        this.vista = vista;
        this.testuale = testuale;
        vista.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mondo.lascia(vista.screenToWorld(e.getPoint()), 0.25);
            }
        });
    }

    public void avvia() {
        ultimo = System.nanoTime();
        timer.start();
    }

    private void stepNext() {
        long adesso = System.nanoTime();
        double dt = (adesso - ultimo) / 1e9;
        ultimo = adesso;
        mondo.stepNext(dt);
        vista.repaint();
        daUltimaRiga += dt;
        if (daUltimaRiga >= 1.0) {
            testuale.aggiorna(mondo);
            daUltimaRiga -= 1.0;
        }
    }
}
