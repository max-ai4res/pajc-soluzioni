package it.unibs.pajc.redolfi.e04;

import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;

/** Il controllore: il mouse diventa lascia(), lo slider e il pulsante diventano proprieta del mondo. */
public class Controllore {

    private final Mondo mondo;
    private final PnlMondo vista;
    private final Timer timer = new Timer(16, e -> stepNext());
    private long ultimo;

    public Controllore(Mondo mondo, PnlMondo vista, JSlider sldGravita, JToggleButton btnPausa) {
        this.mondo = mondo;
        this.vista = vista;
        vista.addPuntoListener(p -> mondo.lascia(p, 0.25));

        // la gravita, nei due versi: lo slider cambia il mondo, il mondo cambia lo slider
        sldGravita.setValue((int) Math.round(mondo.gravita() * 100));
        sldGravita.addChangeListener(
        e -> mondo.setGravita(sldGravita.getValue() / 100.0));
        mondo.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("gravita"))
                sldGravita.setValue((int) Math.round((Double) e.getNewValue() * 100));
        });

        // la pausa, nei due versi
        btnPausa.addActionListener(e -> mondo.setInPausa(btnPausa.isSelected()));
        mondo.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("inPausa")) {
                btnPausa.setSelected((Boolean) e.getNewValue());
                vista.repaint();
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
    }
}
