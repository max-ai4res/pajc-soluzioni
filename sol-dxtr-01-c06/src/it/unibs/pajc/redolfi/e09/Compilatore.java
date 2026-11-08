package it.unibs.pajc.redolfi.e09;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/** Il compilatore su disco della dispensa, per il confronto. */
public class Compilatore {

    private final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final Path cartella = Files.createTempDirectory("compilatore-");
    private final String classPath = System.getProperty("java.class.path");
    private int contatore;

    public Compilatore() throws IOException { }

    public Class<?> compila(String nomeClasse, String sorgente) throws ErroreDiCompilazione {
        JavaFileObject file = new SimpleJavaFileObject(
                URI.create("string:///" + nomeClasse.replace('.', '/') + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean b) { return sorgente; }
        };
        DiagnosticCollector<JavaFileObject> diagnostiche = new DiagnosticCollector<>();
        Path uscita = cartella.resolve("c" + (++contatore));
        try {
            Files.createDirectories(uscita);
            boolean ok = javac.getTask(null, null, diagnostiche,
                    List.of("-d", uscita.toString(), "-classpath", classPath), null, List.of(file)).call();
            if (!ok)
                throw new ErroreDiCompilazione(sorgente, diagnostiche.getDiagnostics());
            URLClassLoader loader = new URLClassLoader(new URL[] { uscita.toUri().toURL() }, getClass().getClassLoader());
            return Class.forName(nomeClasse, true, loader);
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
