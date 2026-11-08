package it.unibs.pajc.redolfi.e07;

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
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Esercizio 6.7: la calcolatrice che impara operatori nuovi a runtime.
 *
 * Idea: gli operatori sono DoubleBinaryOperator in una mappa; "nuovo nome espr"
 * incastona l'espressione in a e b in un modello di classe che implementa
 * DoubleBinaryOperator, la compila con javax.tools in una cartella temporanea, la
 * carica con un URLClassLoader nuovo e la mette nella mappa. L'espressione sta su una
 * riga propria del modello (la 5), cosi' un errore si riporta alla sua colonna.
 * "salva" scrive un file nome.txt con l'espressione per ogni operatore nuovo nella
 * cartella operatori/; all'avvio i file vengono ricompilati.
 *
 * Errore tipico: il modello generato con un refuso (uno storico "return %s: }") fa
 * fallire ogni espressione e l'errore sembra dell'utente: quando non si capisce, si
 * stampa il sorgente generato ("sorgente nome" lo fa).
 */
public class Calcolatrice {

    private static final String MODELLO = """
            package op;
            import static java.lang.Math.*;
            public class %s implements java.util.function.DoubleBinaryOperator {
                public double applyAsDouble(double a, double b) { return
            %s
                ; } }
            """;
    private static final String RIENTRO = "        ";
    private static final Path CARTELLA = Path.of("operatori");

    private final Map<String, DoubleBinaryOperator> operatori = new LinkedHashMap<>();
    private final Map<String, String> sorgenti = new LinkedHashMap<>();     // nome -> espressione
    private final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final Path lavoro = Files.createTempDirectory("calc-");
    private int contatore;

    Calcolatrice() throws IOException {
        operatori.put("+", (a, b) -> a + b);
        operatori.put("-", (a, b) -> a - b);
        operatori.put("*", (a, b) -> a * b);
        operatori.put("/", (a, b) -> a / b);
        if (Files.isDirectory(CARTELLA)) {
            try (Stream<Path> file = Files.list(CARTELLA)) {
                for (Path p : file.filter(f -> f.toString().endsWith(".txt")).toList()) {
                    String nome = p.getFileName().toString().replace(".txt", "");
                    String esito = nuovo(nome, Files.readString(p).strip());
                    System.out.println("ricaricato " + nome + ": " + esito);
                }
            }
        }
    }

    /** Compila l'espressione come operatore 'nome'; restituisce "ok" o il messaggio d'errore. */
    String nuovo(String nome, String espressione) {
        String classe = "Op" + (++contatore);
        String sorgente = MODELLO.formatted(classe, RIENTRO + espressione);
        JavaFileObject file = new SimpleJavaFileObject(java.net.URI.create("string:///op/" + classe + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean b) { return sorgente; }
        };
        DiagnosticCollector<JavaFileObject> diagnostiche = new DiagnosticCollector<>();
        Path uscita = lavoro.resolve(classe);
        try {
            Files.createDirectories(uscita);
            boolean ok = javac.getTask(null, null, diagnostiche, List.of("-d", uscita.toString()), null,
                    List.of(file)).call();
            if (!ok) {
                Diagnostic<?> d = diagnostiche.getDiagnostics().get(0);
                String dove = d.getLineNumber() == 5 ? "colonna " + Math.max(1, d.getColumnNumber() - RIENTRO.length())
                        : "alla fine dell'espressione";
                return dove + ": " + d.getMessage(Locale.ITALIAN).lines().findFirst().orElse("");
            }
            URLClassLoader loader = new URLClassLoader(new URL[] { uscita.toUri().toURL() },
                    getClass().getClassLoader());
            Class<?> k = Class.forName("op." + classe, true, loader);
            operatori.put(nome, (DoubleBinaryOperator) k.getDeclaredConstructor().newInstance());
            sorgenti.put(nome, espressione);
            return "ok";
        } catch (IOException | ReflectiveOperationException e) {
            return "errore interno: " + e;
        }
    }

    void salva() throws IOException {
        Files.createDirectories(CARTELLA);
        for (Map.Entry<String, String> e : sorgenti.entrySet())
            Files.writeString(CARTELLA.resolve(e.getKey() + ".txt"), e.getValue() + "\n");
        System.out.println(sorgenti.size() + " operatori salvati in " + CARTELLA);
    }

    public static void main(String[] args) throws IOException {
        Calcolatrice calc = new Calcolatrice();
        System.out.println("operatori: " + calc.operatori.keySet() + "; es. hyp 3 4, nuovo hyp sqrt(a*a+b*b), salva, fine");
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("> ");
            while (in.hasNextLine()) {
                String[] parole = in.nextLine().strip().split("\\s+", 3);
                try {
                    switch (parole[0]) {
                        case "" -> { }
                        case "fine" -> { return; }
                        case "salva" -> calc.salva();
                        case "nuovo" -> System.out.println(calc.nuovo(parole[1], parole[2]));
                        case "sorgente" -> System.out.println(MODELLO.formatted("OpN", RIENTRO + calc.sorgenti.get(parole[1])));
                        default -> {
                            DoubleBinaryOperator op = calc.operatori.get(parole[0]);
                            if (op == null)
                                System.out.println("operatore sconosciuto: " + parole[0]);
                            else
                                System.out.println(op.applyAsDouble(Double.parseDouble(parole[1]), Double.parseDouble(parole[2])));
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("uso: <operatore> <a> <b>, oppure nuovo <nome> <espressione in a e b>");
                }
                System.out.print("> ");
            }
        }
    }
}
