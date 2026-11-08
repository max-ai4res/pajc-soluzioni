package it.unibs.pajc.redolfi.e09;

import java.util.function.Supplier;

/**
 * Esercizio 6.9 (il programma): cento piccole classi con il compilatore su disco e con
 * quello in memoria, con la prima compilazione e la media delle successive.
 *
 * Numeri tipici: la PRIMA compilazione del processo costa 150-400 ms qualunque sia il
 * compilatore che la fa (e' javac che si inizializza e carica le proprie classi: il
 * secondo compilatore misurato trova gia' tutto caldo, e la sua "prima" e' piccola);
 * le successive 5-10 ms su disco e 3-6 ms in memoria. Il disco pesa, ma il grosso e'
 * il lavoro del compilatore, non la scrittura dei file. La classe di prova ha una
 * classe annidata e una lambda, cosi' il compilatore in memoria deve gestire tre
 * .class per compilazione. Con "memoria" come primo argomento l'ordine si inverte.
 */
public class Confronto {

    private static final String MODELLO = """
            package prova;
            public class %s implements java.util.function.Supplier<String> {
                class Interna { String s = "annidata"; }
                public String get() {
                    java.util.function.Supplier<String> l = () -> new Interna().s;
                    return "%s:" + l.get();
                }
            }
            """;

    public static void main(String[] args) throws Exception {
        Compilatore disco = new Compilatore();
        CompilatoreInMemoria memoria = new CompilatoreInMemoria();
        Compilazione suDisco = i -> {
            Class<?> k = disco.compila("prova.D" + i, MODELLO.formatted("D" + i, "D" + i));
            return ((Supplier<?>) k.getDeclaredConstructor().newInstance()).get();
        };
        Compilazione inMemoria = i -> memoria.istanzia("prova.M" + i, MODELLO.formatted("M" + i, "M" + i), Supplier.class).get();
        if (args.length > 0 && args[0].equals("memoria")) {
            misura("in memoria", inMemoria);
            misura("su disco", suDisco);
        } else {
            misura("su disco", suDisco);
            misura("in memoria", inMemoria);
        }
    }

    interface Compilazione { Object esegui(int i) throws Exception; }

    static void misura(String nome, Compilazione c) throws Exception {
        long t0 = System.nanoTime();
        Object primo = c.esegui(0);
        long prima = (System.nanoTime() - t0) / 1_000_000;
        t0 = System.nanoTime();
        for (int i = 1; i < 100; i++)
            c.esegui(i);
        double media = (System.nanoTime() - t0) / 99 / 1e6;
        System.out.printf("%-12s prima %4d ms, media delle successive %.1f ms  (%s)%n", nome, prima, media, primo);
    }
}
