package it.unibs.pajc.redolfi.e02;

import javax.swing.event.EventListenerList;

/**
 * Il modello: il tempo entra con stepNext(dt), i tick partono ogni decimo di secondo
 * misurato (anche piu' di uno in un passo lungo), i giri quando lo chiede l'utente.
 *
 * I listener stanno in una EventListenerList. La prima versione, con una
 * List<CronometroListener> e List.copyOf alla notifica, e' equivalente: cambia solo che
 * EventListenerList puo' tenere listener di piu' tipi (qui ce n'e' uno) e che la copia
 * la fa lei in getListeners. Con un tipo solo la lista a mano e' piu' semplice; con un
 * componente che ha ActionListener, ChangeListener e altri, EventListenerList e' piu'
 * comoda.
 */
public class Cronometro {

    private static final double TICK = 0.1;

    private double totale, inizioGiro, daUltimoTick;
    private int giri;
    private boolean inCorso;
    private final EventListenerList listeners = new EventListenerList();

    public void addCronometroListener(CronometroListener l) {
        listeners.add(CronometroListener.class, l);
    }

    public void removeCronometroListener(CronometroListener l) {
        listeners.remove(CronometroListener.class, l);
    }

    public double tempo()    { return totale; }
    public boolean inCorso() { return inCorso; }

    public void avvia() { inCorso = true; }
    public void ferma() { inCorso = false; }

    public void azzera() {
        totale = inizioGiro = daUltimoTick = 0;
        giri = 0;
        for (CronometroListener l : listeners.getListeners(CronometroListener.class))
            l.tick(0);
    }

    public void giro() {
        if (!inCorso)
            return;
        giri++;
        double tempoGiro = totale - inizioGiro;
        inizioGiro = totale;
        for (CronometroListener l : listeners.getListeners(CronometroListener.class))
            l.giro(giri, tempoGiro, totale);
    }

    public void stepNext(double dt) {
        if (!inCorso)
            return;
        totale += dt;
        daUltimoTick += dt;
        while (daUltimoTick >= TICK) {
            daUltimoTick -= TICK;
            for (CronometroListener l : listeners.getListeners(CronometroListener.class))
                l.tick(totale - daUltimoTick);
        }
    }
}
