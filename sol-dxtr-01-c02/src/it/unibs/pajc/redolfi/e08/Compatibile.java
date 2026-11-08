package it.unibs.pajc.redolfi.e08;

import java.util.List;
import java.util.Map;

/**
 * Esercizio 2.8: compatibile(Object o, String nomeTipo).
 *
 * Idea: tradurre il nome in un Class<?> e poi chiedere isInstance(o). I primitivi non
 * hanno un nome binario accettato da forName, quindi stanno in una mappa; un valore
 * si considera compatibile con "int" se e' un Integer (come farebbe l'unboxing). Gli
 * array scritti come "Tipo[]" si traducono nel nome binario "[LTipo;" (o "[I" per i
 * primitivi), ricorsivamente per piu' dimensioni. Un nome inesistente vale false.
 *
 * Errore tipico: passare "int" o "java.lang.Object[]" a forName cosi' com'e':
 * ClassNotFoundException in entrambi i casi.
 */
public class Compatibile {

    private static final Map<String, Class<?>> PRIMITIVI = Map.of(
            "int", Integer.class, "long", Long.class, "double", Double.class, "float", Float.class,
            "boolean", Boolean.class, "char", Character.class, "byte", Byte.class, "short", Short.class);

    private static final Map<String, String> CODICI = Map.of(
            "int", "I", "long", "J", "double", "D", "float", "F",
            "boolean", "Z", "char", "C", "byte", "B", "short", "S");

    public static boolean compatibile(Object o, String nomeTipo) {
        Class<?> tipo = risolvi(nomeTipo);
        return tipo != null && tipo.isInstance(o);
    }

    /** Il Class<?> di un nome scritto come nel sorgente, o null se non esiste. */
    static Class<?> risolvi(String nome) {
        if (PRIMITIVI.containsKey(nome))
            return PRIMITIVI.get(nome);
        try {
            return Class.forName(nomeBinario(nome));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** "java.lang.Object[]" -> "[Ljava.lang.Object;", "int[][]" -> "[[I", altrimenti invariato. */
    static String nomeBinario(String nome) {
        if (!nome.endsWith("[]"))
            return nome;
        String elemento = nome.substring(0, nome.length() - 2);
        if (elemento.endsWith("[]"))
            return "[" + nomeBinario(elemento);
        if (CODICI.containsKey(elemento))
            return "[" + CODICI.get(elemento);
        return "[L" + elemento + ";";
    }

    public static void main(String[] args) {
        Object[][] prove = {
                { 42, "int" }, { 42, "java.lang.Number" }, { 42, "long" }, { "ciao", "java.lang.CharSequence" },
                { new String[] { "a" }, "java.lang.Object[]" }, { new int[] { 1 }, "int[]" },
                { new int[] { 1 }, "java.lang.Object[]" }, { List.of(1), "java.util.Collection" },
                { 3.5, "java.lang.Comparable" }, { "x", "tipo.Inesistente" }, { null, "java.lang.String" } };
        for (Object[] p : prove)
            System.out.printf("%-14s %-24s %s%n", descrivi(p[0]), p[1], compatibile(p[0], (String) p[1]));
    }

    private static String descrivi(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName() + " " + (o.getClass().isArray() ? "" : o);
    }
}
