package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Esercizio 6.8: un piccolo REPL per espressioni su double.
 *
 * Idea (la seconda strada del suggerimento): le variabili vivono in una
 * Map<String, Double>; ogni riga diventa una classe che implementa
 * Function<Map<String, Double>, Double>, il cui metodo apply dichiara una variabile
 * locale "double nome = ctx.get("nome");" per ogni variabile nota e poi valuta
 * l'espressione. Un'assegnazione "nome = espr" valuta l'espressione e scrive nella
 * mappa; un'espressione da sola si stampa. Ogni classe ha un nome nuovo e un loader
 * nuovo.
 *
 * Errore tipico: dichiarare nel modello tutte le variabili anche quando l'espressione
 * non le usa e' innocuo; dichiararle con un nome che e' una parola riservata di Java
 * (double for = ...) no: i nomi delle variabili vanno validati.
 */
public class Repl {

    private static final String MODELLO = """
            package repl;
            import java.util.Map;
            import java.util.function.Function;
            import static java.lang.Math.*;
            public class %s implements Function<Map<String, Double>, Double> {
                public Double apply(Map<String, Double> ctx) {
            %s
                    return (double) (
            %s
                    );
                }
            }
            """;

    private final Map<String, Double> variabili = new LinkedHashMap<>();
    private final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final Path lavoro = Files.createTempDirectory("repl-");
    private int contatore;

    Repl() throws IOException { }

    @SuppressWarnings("unchecked")
    double valuta(String espressione) throws Exception {
        String classe = "E" + (++contatore);
        StringBuilder dichiarazioni = new StringBuilder();
        for (String nome : variabili.keySet())
            dichiarazioni.append("        double ").append(nome).append(" = ctx.get(\"").append(nome).append("\");\n");
        String sorgente = MODELLO.formatted(classe, dichiarazioni, "            " + espressione);
        JavaFileObject file = new SimpleJavaFileObject(java.net.URI.create("string:///repl/" + classe + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean b) { return sorgente; }
        };
        DiagnosticCollector<JavaFileObject> diagnostiche = new DiagnosticCollector<>();
        Path uscita = lavoro.resolve(classe);
        Files.createDirectories(uscita);
        if (!javac.getTask(null, null, diagnostiche, List.of("-d", uscita.toString()), null, List.of(file)).call())
            throw new IllegalArgumentException(diagnostiche.getDiagnostics().get(0).getMessage(Locale.ITALIAN)
                    .lines().findFirst().orElse("errore"));
        URLClassLoader loader = new URLClassLoader(new URL[] { uscita.toUri().toURL() }, getClass().getClassLoader());
        Class<?> k = Class.forName("repl." + classe, true, loader);
        var f = (Function<Map<String, Double>, Double>) k.getDeclaredConstructor().newInstance();
        return f.apply(variabili);
    }

    public static void main(String[] args) throws Exception {
        Repl repl = new Repl();
        System.out.println("espressioni su double; r = 2, poi PI * r * r; fine per uscire");
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("> ");
            while (in.hasNextLine()) {
                String riga = in.nextLine().strip();
                if (riga.equals("fine"))
                    break;
                try {
                    if (riga.isEmpty()) {
                        // niente
                    } else if (riga.matches("[a-zA-Z_]\\w*\\s*=[^=].*")) {
                        String nome = riga.substring(0, riga.indexOf('=')).strip();
                        double v = repl.valuta(riga.substring(riga.indexOf('=') + 1));
                        repl.variabili.put(nome, v);
                        System.out.println(nome + " = " + v);
                    } else {
                        System.out.println(repl.valuta(riga));
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("errore: " + e.getMessage());
                }
                System.out.print("> ");
            }
        }
    }
}
