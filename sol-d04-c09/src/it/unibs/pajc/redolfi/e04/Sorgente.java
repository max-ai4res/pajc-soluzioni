package it.unibs.pajc.redolfi.e04;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Palestra 9.4: una sorgente di eventi con i listener, in due varianti.
 *
 * Con ArrayList (modo "arraylist") la notifica itera sulla lista mentre altri thread
 * la modificano, e il listener che si rimuove da solo la modifica *sullo stesso thread*
 * della notifica: ConcurrentModificationException, dal thread che sta notificando.
 * Un synchronized attorno al for non basta: il monitor e' rientrante e lascia passare
 * la remove fatta dal listener dentro l'iterazione.
 *
 * Con CopyOnWriteArrayList la notifica itera su un'istantanea dell'array: una remove
 * (da chiunque) crea un array nuovo e non tocca quello in uso. Il listener che si
 * rimuove riceve comunque l'evento in corso (sta nell'istantanea) e nessuno dei
 * successivi: e' il comportamento giusto, perche' la rimozione vale "da ora in poi".
 */
public class Sorgente {
    private final List<Consumer<String>> listener;

    public Sorgente(boolean copyOnWrite) {
        listener = copyOnWrite ? new CopyOnWriteArrayList<>() : new ArrayList<>();
    }

    public void addListener(Consumer<String> l) { listener.add(l); }
    public void removeListener(Consumer<String> l) { listener.remove(l); }

    public void notifica(String evento) {
        for (Consumer<String> l : listener) l.accept(evento);
    }
}
