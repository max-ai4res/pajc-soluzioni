package it.unibs.pajc.redolfi.e07;

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

/**
 * Compila una classe Java da una stringa e la carica con un class loader nuovo.
 *
 * Ogni compilazione scrive i .class in una sottocartella propria della cartella di
 * lavoro e li carica con un URLClassLoader nuovo, figlio del loader di questa classe:
 * cosi' lo stesso nome di classe si puo' ricompilare quante volte si vuole, e la
 * classe compilata vede le classi del programma (Funzione, @Plot).
 */
public class Compilatore {

    private final JavaCompiler javac;
    private final Path cartella;
    private final String classPath;
    private int contatore;
    private Path ultimaUscita;

    public Compilatore() throws IOException {
        javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null)
            throw new IllegalStateException("nessun compilatore: il programma gira su un JRE, serve un JDK");
        cartella = Files.createTempDirectory("compilatore-");
        classPath = System.getProperty("java.class.path");
    }

    /** Compila il sorgente della classe con quel nome binario e restituisce la classe caricata. */
    public Class<?> compila(String nomeClasse, String sorgente) throws ErroreDiCompilazione {
        JavaFileObject file = new SorgenteInMemoria(nomeClasse, sorgente);
        DiagnosticCollector<JavaFileObject> diagnostiche = new DiagnosticCollector<>();
        Path uscita = cartella.resolve("c" + (++contatore));
        ultimaUscita = uscita;
        List<String> opzioni = List.of("-d", uscita.toString(), "-classpath", classPath);
        try {
            Files.createDirectories(uscita);
            boolean ok = javac.getTask(null, null, diagnostiche, opzioni, null, List.of(file)).call();
            if (!ok)
                throw new ErroreDiCompilazione(sorgente, diagnostiche.getDiagnostics());
            URLClassLoader loader = new URLClassLoader(new URL[] { uscita.toUri().toURL() },
                    getClass().getClassLoader());
            return Class.forName(nomeClasse, true, loader);
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("compilato ma non caricabile: " + nomeClasse, e);
        }
    }

    /** La cartella con i .class dell'ultima compilazione riuscita (per impacchettarli). */
    public Path ultimaUscita() {
        return ultimaUscita;
    }

    /** Come compila, ma restituisce gia' un'istanza del tipo chiesto. */
    public <T> T istanzia(String nomeClasse, String sorgente, Class<T> tipo) throws ErroreDiCompilazione {
        Class<?> classe = compila(nomeClasse, sorgente);
        if (!tipo.isAssignableFrom(classe))
            throw new IllegalArgumentException(nomeClasse + " non e' un " + tipo.getSimpleName());
        try {
            return tipo.cast(classe.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("impossibile istanziare " + nomeClasse, e);
        }
    }

    /** Un sorgente che sta in una stringa invece che in un file. */
    private static final class SorgenteInMemoria extends SimpleJavaFileObject {
        private final String codice;

        SorgenteInMemoria(String nomeClasse, String codice) {
            super(URI.create("string:///" + nomeClasse.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.codice = codice;
        }

        @Override
        public CharSequence getCharContent(boolean ignoraErroriDiCodifica) {
            return codice;
        }
    }
}
