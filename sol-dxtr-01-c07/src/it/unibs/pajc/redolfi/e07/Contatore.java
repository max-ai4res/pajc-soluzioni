package it.unibs.pajc.redolfi.e07;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

/** Incarta una Funzione in un proxy che conta le chiamate a f. */
public class Contatore {

    private final AtomicLong chiamate = new AtomicLong();

    public Funzione conta(Funzione vera) {
        return (Funzione) Proxy.newProxyInstance(Funzione.class.getClassLoader(), new Class<?>[] { Funzione.class },
            (proxy, metodo, argomenti) -> {
                if (metodo.getName().equals("f"))
                    chiamate.incrementAndGet();
                return metodo.invoke(vera, argomenti);
            });
    }

    public long chiamate() {
        return chiamate.get();
    }

    public void azzera() {
        chiamate.set(0);
    }
}
