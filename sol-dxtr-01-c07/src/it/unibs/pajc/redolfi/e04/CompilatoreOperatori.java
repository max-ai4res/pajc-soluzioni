package it.unibs.pajc.redolfi.e04;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/** Compila il corpo di un operatore in a e b in un DoubleBinaryOperator; errori riferiti alla riga del corpo. */
public class CompilatoreOperatori {

    private static final String MODELLO = """
            package op;
            import static java.lang.Math.*;
            public class %s implements java.util.function.DoubleBinaryOperator {
                public double applyAsDouble(double a, double b) {
                    return
            %s
                    ;
                }
            }
            """;
    private static final int RIGA_CORPO = 6;
    private static final String RIENTRO = "        ";

    /** Il corpo non compila: il messaggio e' gia' sulle coordinate del corpo. */
    public static class ErroreDiCompilazione extends Exception {
        public ErroreDiCompilazione(String messaggio) { super(messaggio); }
    }

    private final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final Path lavoro;
    private int contatore;

    public CompilatoreOperatori() throws IOException {
        if (javac == null)
            throw new IllegalStateException("nessun compilatore: serve un JDK");
        lavoro = Files.createTempDirectory("calc-");
    }

    public DoubleBinaryOperator compila(String corpo) throws ErroreDiCompilazione {
        String classe = "Op" + (++contatore);
        String sorgente = MODELLO.formatted(classe, RIENTRO + corpo.strip());
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///op/" + classe + ".java"),
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
                String messaggio = d.getMessage(Locale.ITALIAN).lines().findFirst().orElse("errore");
                if (d.getLineNumber() == RIGA_CORPO)
                    throw new ErroreDiCompilazione("colonna " + Math.max(1, d.getColumnNumber() - RIENTRO.length()) + ": " + messaggio);
                throw new ErroreDiCompilazione("alla fine del corpo: " + messaggio);
            }
            URLClassLoader loader = new URLClassLoader(new URL[] { uscita.toUri().toURL() }, getClass().getClassLoader());
            Class<?> k = Class.forName("op." + classe, true, loader);
            return (DoubleBinaryOperator) k.getDeclaredConstructor().newInstance();
        } catch (IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("errore interno: " + e, e);
        }
    }
}
