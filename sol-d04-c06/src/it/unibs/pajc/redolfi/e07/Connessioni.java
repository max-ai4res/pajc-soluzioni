package it.unibs.pajc.redolfi.e07;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Un servizio che regge al massimo tre connessioni: un Semaphore con tre permessi. */
public class Connessioni {
    private final Semaphore permessi;

    public Connessioni(int max) { permessi = new Semaphore(max, true); }

    /** Apre una connessione aspettando finche' non c'e' posto. */
    public void apri() throws InterruptedException { permessi.acquire(); }

    /** Prova ad aprire entro il tempo dato; false se si rinuncia. */
    public boolean apri(long timeoutMs) throws InterruptedException {
        return permessi.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void chiudi() { permessi.release(); }

    public int disponibili() { return permessi.availablePermits(); }
}
