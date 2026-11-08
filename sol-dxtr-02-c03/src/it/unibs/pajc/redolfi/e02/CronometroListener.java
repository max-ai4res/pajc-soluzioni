package it.unibs.pajc.redolfi.e02;

import java.util.EventListener;

/** Chi ascolta il cronometro. Estende EventListener perche' EventListenerList lo richiede. */
public interface CronometroListener extends EventListener {

    default void tick(double tempo) { }

    default void giro(int numero, double tempoGiro, double tempoTotale) { }
}
