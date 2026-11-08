package it.unibs.pajc.redolfi.e09;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.tools.ToolProvider;

/**
 * Esercizio 5.9: ricarica a caldo.
 *
 * Idea: le due versioni di Saluto si scrivono e si compilano al volo in due cartelle
 * temporanee (con javax.tools, l'argomento del capitolo seguente: qui e' solo un modo
 * comodo per avere due .class con lo stesso nome). Due URLClassLoader, uno per
 * cartella, con il loader dell'applicazione come padre: le classi sono diverse (==
 * falso), un'istanza non e' assegnabile all'altra classe, ma entrambe sono
 * Supplier<String>, perche' Supplier viene dal padre. Poi il primo loader, la sua
 * classe e la sua istanza si abbandonano (tre null) e una WeakReference mostra che la
 * classe viene scaricata; nel secondo esperimento un'istanza resta in una lista e la
 * classe non si scarica.
 *
 * Errore tipico: dimenticare uno dei tre riferimenti (di solito l'istanza) e concludere
 * che "le classi non si scaricano mai".
 */
public class RicaricaACaldo {

    public static void main(String[] args) throws Exception {
        Path v1 = compila("versione 1"), v2 = compila("versione 2");
        ClassLoader padre = RicaricaACaldo.class.getClassLoader();
        URLClassLoader l1 = new URLClassLoader("L1", new URL[] { v1.toUri().toURL() }, padre);
        URLClassLoader l2 = new URLClassLoader("L2", new URL[] { v2.toUri().toURL() }, padre);
        Class<?> c1 = Class.forName("Saluto", true, l1), c2 = Class.forName("Saluto", true, l2);
        Object s1 = c1.getDeclaredConstructor().newInstance(), s2 = c2.getDeclaredConstructor().newInstance();
        System.out.println("stesso nome: " + c1.getName().equals(c2.getName()) + ", stessa classe: " + (c1 == c2));
        System.out.println("c2.isInstance(s1): " + c2.isInstance(s1));
        System.out.println("entrambe Supplier: " + (s1 instanceof Supplier) + " " + (s2 instanceof Supplier));
        System.out.println("dicono: " + ((Supplier<?>) s1).get() + " / " + ((Supplier<?>) s2).get());

        // scarico: tre null e il garbage collector
        WeakReference<Class<?>> ref1 = new WeakReference<>(c1);
        l1.close();
        l1 = null; c1 = null; s1 = null;
        System.out.println("classe v1 scaricata: " + aspettaScarico(ref1));

        // non si scarica se qualcuno tiene un'istanza
        List<Object> tenute = new ArrayList<>();
        tenute.add(s2);
        WeakReference<Class<?>> ref2 = new WeakReference<>(c2);
        l2.close();
        l2 = null; c2 = null; s2 = null;
        System.out.println("classe v2 scaricata con un'istanza in una lista: " + aspettaScarico(ref2));
        tenute.clear();
        System.out.println("classe v2 scaricata dopo aver svuotato la lista: " + aspettaScarico(ref2));
    }

    static boolean aspettaScarico(WeakReference<?> ref) throws InterruptedException {
        for (int i = 0; i < 20 && ref.get() != null; i++) {
            System.gc();
            Thread.sleep(50);
        }
        return ref.get() == null;
    }

    /** Scrive e compila Saluto in una cartella temporanea; restituisce la cartella. */
    static Path compila(String messaggio) throws Exception {
        Path cartella = Files.createTempDirectory("saluto-");
        Path sorgente = cartella.resolve("Saluto.java");
        Files.writeString(sorgente, "public class Saluto implements java.util.function.Supplier<String> {"
                + " public String get() { return \"" + messaggio + "\"; } }");
        int esito = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", cartella.toString(), sorgente.toString());
        if (esito != 0)
            throw new IllegalStateException("compilazione fallita");
        return cartella;
    }
}
