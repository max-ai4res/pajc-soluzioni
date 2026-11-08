package it.unibs.pajc.redolfi.e08;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Esercizio 3.8: una shell per gli oggetti.
 *
 * Idea: una riga "nome.metodo(arg, arg)" si spezza con un'espressione regolare; si
 * cerca l'oggetto nella mappa e, fra i metodi pubblici della sua classe, uno con quel
 * nome e tanti parametri quanti gli argomenti scritti. Ogni argomento (una stringa) si
 * converte nel tipo del parametro con una piccola tabella (int, double, boolean,
 * String, Object...); se la conversione fallisce si prova il metodo omonimo seguente
 * (String ha substring(int) e substring(int, int), List ha add(E) e add(int, E)). Poi
 * invoke, con InvocationTargetException tradotta nel messaggio della causa.
 *
 * Errore tipico: cercare il metodo con getMethod(nome, String.class, ...) perche' gli
 * argomenti arrivano come stringhe: NoSuchMethodException per quasi tutto. I tipi
 * della firma sono quelli del metodo, e la conversione tocca a noi.
 */
public class ShellOggetti {

    private static final Pattern RIGA = Pattern.compile("(\\w+)\\.(\\w+)\\((.*)\\)");

    private final Map<String, Object> oggetti = new LinkedHashMap<>();

    public static void main(String[] args) {
        ShellOggetti shell = new ShellOggetti();
        shell.oggetti.put("s", "reflection");
        shell.oggetti.put("c", new Circle(2));
        shell.oggetti.put("l", new ArrayList<String>());
        shell.oggetti.put("sb", new StringBuilder());
        System.out.println("oggetti: " + shell.oggetti.keySet() + "; esempio: s.substring(2, 5); fine per uscire");
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("> ");
            while (in.hasNextLine()) {
                String riga = in.nextLine().strip();
                if (riga.equals("fine"))
                    break;
                if (!riga.isEmpty())
                    System.out.println(shell.esegui(riga));
                System.out.print("> ");
            }
        }
    }

    String esegui(String riga) {
        Matcher m = RIGA.matcher(riga);
        if (!m.matches())
            return "non capisco: scrivere oggetto.metodo(arg, ...)";
        Object bersaglio = oggetti.get(m.group(1));
        if (bersaglio == null)
            return "oggetto sconosciuto: " + m.group(1) + " (ci sono " + oggetti.keySet() + ")";
        String nome = m.group(2);
        String[] argomenti = m.group(3).isBlank() ? new String[0] : m.group(3).split("\\s*,\\s*");
        List<Method> candidati = Arrays.stream(bersaglio.getClass().getMethods())
                .filter(k -> k.getName().equals(nome) && k.getParameterCount() == argomenti.length)
                .toList();
        if (candidati.isEmpty())
            return "nessun metodo pubblico " + nome + " con " + argomenti.length + " argomenti in "
                    + bersaglio.getClass().getSimpleName();
        for (Method metodo : candidati) {
            Object[] valori = converti(argomenti, metodo.getParameterTypes());
            if (valori == null)
                continue;                                   // gli argomenti non sono di questo tipo
            try {
                Object risultato = metodo.invoke(bersaglio, valori);
                return metodo.getReturnType() == void.class ? "(fatto)" : String.valueOf(risultato);
            } catch (InvocationTargetException e) {
                return "il metodo ha lanciato " + e.getCause();
            } catch (IllegalAccessException e) {
                return "metodo non accessibile: " + e.getMessage();
            }
        }
        return "gli argomenti non corrispondono a nessuna versione di " + nome;
    }

    /** Converte ogni stringa nel tipo del parametro; null se qualcuna non si puo' convertire. */
    private static Object[] converti(String[] argomenti, Class<?>[] tipi) {
        Object[] valori = new Object[argomenti.length];
        try {
            for (int i = 0; i < argomenti.length; i++)
                valori[i] = converti(argomenti[i], tipi[i]);
            return valori;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Object converti(String s, Class<?> tipo) {
        if (tipo == int.class || tipo == Integer.class) return Integer.parseInt(s);
        if (tipo == long.class || tipo == Long.class) return Long.parseLong(s);
        if (tipo == double.class || tipo == Double.class) return Double.parseDouble(s);
        if (tipo == boolean.class || tipo == Boolean.class) {
            if (s.equals("true") || s.equals("false")) return Boolean.parseBoolean(s);
            throw new IllegalArgumentException(s);
        }
        if (tipo == char.class && s.length() == 1) return s.charAt(0);
        if (tipo == String.class || tipo == CharSequence.class || tipo == Object.class) return s;
        throw new IllegalArgumentException("tipo non gestito: " + tipo.getSimpleName());
    }

    static class Circle {
        private final double radius;
        public Circle(double radius) { this.radius = radius; }
        public double area() { return Math.PI * radius * radius; }
        public double scala(double k) { return radius * k; }
        @Override public String toString() { return "Circle(" + radius + ")"; }
    }
}
