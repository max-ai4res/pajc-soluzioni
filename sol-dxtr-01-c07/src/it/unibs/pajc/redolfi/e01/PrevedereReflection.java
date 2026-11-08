package it.unibs.pajc.redolfi.e01;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.JComponent;

/**
 * Esercizio 7.1: prevedere la reflection.
 *
 * Risposte attese:
 * (a) i nomi dei metodi pubblici di JComponent (e delle superclassi) che iniziano per
 *     "setV", senza duplicati e in ordine: setValid? no (e' isValid); si trovano
 *     setVerifyInputWhenFocusTarget, setVisible; l'elenco esatto dipende dalla
 *     versione del JDK, e il punto e' che getMethods include gli ereditati (setVisible
 *     e' di Component).
 * (b) false: getDeclaredMethods conta anche i privati di String e non gli ereditati;
 *     getMethods conta i pubblici compresi quelli di Object: numeri diversi.
 * (c) IllegalArgumentException "argument type mismatch": 7.0 e' un Double, il
 *     parametro e' int, e invoke non converte.
 * (d) stampa 0: la classe si trova, il costruttore vuoto c'e', size() restituisce 0
 *     incartato in un Integer.
 */
public class PrevedereReflection {

    public static void main(String[] args) {
        prova("a", () -> Arrays.stream(JComponent.class.getMethods())
                .filter(m -> m.getName().startsWith("setV"))
                .map(Method::getName).distinct().sorted().forEach(System.out::println));
        prova("b", () -> System.out.println(String.class.getDeclaredMethods().length
                == String.class.getMethods().length));
        prova("c", () -> {
            Method m = Math.class.getMethod("max", int.class, int.class);
            System.out.println(m.invoke(null, 3, 7.0));
        });
        prova("d", () -> {
            Class<?> k = Class.forName("java.util.ArrayList");
            Object o = k.getDeclaredConstructor().newInstance();
            System.out.println(o.getClass().getMethod("size").invoke(o));
        });
    }

    interface Frammento { void esegui() throws Exception; }

    static void prova(String nome, Frammento f) {
        System.out.println("(" + nome + ")");
        try {
            f.esegui();
        } catch (Exception e) {
            System.out.println("  -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
