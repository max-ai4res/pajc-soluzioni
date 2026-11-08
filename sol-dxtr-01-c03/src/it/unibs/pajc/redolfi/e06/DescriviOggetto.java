package it.unibs.pajc.redolfi.e06;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Esercizio 3.6: un toString per tutti.
 *
 * Idea: risalire la gerarchia con getSuperclass() fermandosi prima di Object, e per
 * ogni classe prendere getDeclaredFields(), saltare gli static, aprire i privati con
 * setAccessible e leggere il valore con get. Un valore di una classe "nostra" (non del
 * JDK: il suo package non inizia per java.) si descrive ricorsivamente; gli altri con
 * toString. Gli oggetti gia' visitati stanno in un insieme per identita', cosi' un
 * riferimento circolare stampa "..." invece di esplodere.
 *
 * Errore tipico: getDeclaredFields() sulla sola classe dell'oggetto (si perdono i campi
 * privati delle superclassi), e nessuna difesa dai cicli (StackOverflowError).
 */
public class DescriviOggetto {

    public static String descrivi(Object o) {
        return descrivi(o, java.util.Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String descrivi(Object o, Set<Object> visitati) {
        if (o == null)
            return "null";
        Class<?> c = o.getClass();
        if (c.isArray())
            return arrayToString(o, visitati);
        if (!nostra(c))
            return String.valueOf(o);             // String, Integer, List...: il loro toString
        if (!visitati.add(o))
            return c.getSimpleName() + "[...]";   // gia' visto: riferimento circolare
        List<String> parti = new ArrayList<>();
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()))
                    continue;
                f.setAccessible(true);
                try {
                    parti.add(f.getName() + "=" + descrivi(f.get(o), visitati));
                } catch (IllegalAccessException e) {
                    parti.add(f.getName() + "=?");
                }
            }
        }
        return c.getSimpleName() + "[" + String.join(", ", parti) + "]";
    }

    private static boolean nostra(Class<?> c) {
        String p = c.getPackageName();
        return !p.startsWith("java.") && !p.startsWith("javax.") && !c.isEnum();
    }

    private static String arrayToString(Object a, Set<Object> visitati) {
        int n = java.lang.reflect.Array.getLength(a);
        List<String> parti = new ArrayList<>();
        for (int i = 0; i < n; i++)
            parti.add(descrivi(java.lang.reflect.Array.get(a, i), visitati));
        return "[" + String.join(", ", parti) + "]";
    }

    // --- tre classi di prova

    static class Circle {
        private final double radius;
        Circle(double radius) { this.radius = radius; }
    }

    static class Indirizzo {
        private String via;
        private int civico;
        Indirizzo(String via, int civico) { this.via = via; this.civico = civico; }
    }

    static class Persona {
        private static int creati = 0;
        private String nome;
        private int anni;
        Persona(String nome, int anni) { this.nome = nome; this.anni = anni; creati++; }
    }

    static class Studente extends Persona {
        private Indirizzo indirizzo;
        private List<String> esami;
        private Studente amico;
        Studente(String nome, int anni, Indirizzo indirizzo, List<String> esami) {
            super(nome, anni);
            this.indirizzo = indirizzo;
            this.esami = esami;
        }
    }

    public static void main(String[] args) {
        System.out.println(descrivi(new Circle(3)));
        Studente ada = new Studente("Ada", 36, new Indirizzo("via Branze", 38), List.of("PAJC", "Analisi"));
        System.out.println(descrivi(ada));
        Studente bob = new Studente("Bob", 22, null, List.of());
        ada.amico = bob;
        bob.amico = ada;                            // ciclo
        System.out.println(descrivi(ada));
        System.out.println(descrivi(new int[] { 1, 2, 3 }) + " " + descrivi("ciao") + " " + descrivi(null));
    }
}
