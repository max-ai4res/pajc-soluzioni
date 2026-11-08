package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Esercizio 6.6: tutte le diagnostiche di un file .java, con la riga e il cursore.
 *
 * Idea: il gestore dei file standard sa aprire un .java dal disco
 * (getJavaFileObjects); -Xlint:all accende tutti gli avvisi; le diagnostiche si
 * stampano con tipo, riga, colonna e messaggio, seguite dalla riga del sorgente e da
 * un ^ sotto la colonna (le tabulazioni della riga si ricopiano davanti al ^ per
 * tenerlo allineato). Se compila, i .class prodotti nella cartella temporanea danno
 * i nomi delle classi, comprese quelle annidate (Esterna$Interna.class).
 *
 * Esempio: java Diagnostiche res/e06/Rotta.java
 *
 * Errore tipico: contare la colonna come se le tabulazioni fossero otto spazi:
 * il compilatore le conta come un carattere.
 */
public class Diagnostiche {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "res/e06/Rotta.java");
        List<String> righe = Files.readAllLines(file);
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> raccolta = new DiagnosticCollector<>();
        Path uscita = Files.createTempDirectory("diagnostiche-");
        try (StandardJavaFileManager gestore = javac.getStandardFileManager(raccolta, null, null)) {
            Iterable<? extends JavaFileObject> sorgenti = gestore.getJavaFileObjects(file);
            List<String> opzioni = List.of("-d", uscita.toString(), "-Xlint:all");
            boolean ok = javac.getTask(null, gestore, raccolta, opzioni, null, sorgenti).call();
            for (Diagnostic<? extends JavaFileObject> d : raccolta.getDiagnostics()) {
                System.out.printf("%s %d:%d %s%n", d.getKind(), d.getLineNumber(), d.getColumnNumber(),
                        d.getMessage(null).lines().findFirst().orElse(""));
                if (d.getLineNumber() > 0) {
                    String riga = righe.get((int) d.getLineNumber() - 1);
                    System.out.println("    " + riga);
                    StringBuilder cursore = new StringBuilder("    ");
                    for (int i = 0; i < d.getColumnNumber() - 1 && i < riga.length(); i++)
                        cursore.append(riga.charAt(i) == '\t' ? '\t' : ' ');
                    System.out.println(cursore + "^");
                }
            }
            if (ok) {
                System.out.print("compilato; classi prodotte:");
                try (Stream<Path> classi = Files.walk(uscita)) {
                    classi.filter(p -> p.toString().endsWith(".class"))
                          .map(p -> uscita.relativize(p).toString().replace(".class", "").replace('/', '.'))
                          .sorted().forEach(n -> System.out.print(" " + n));
                }
                System.out.println();
            }
        }
    }
}
