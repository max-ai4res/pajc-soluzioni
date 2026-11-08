package it.unibs.pajc.redolfi.e09;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Esercizio 6.9: il compilatore tutto in memoria, con la stessa interfaccia del
 * Compilatore su disco (compila, istanzia, ErroreDiCompilazione).
 *
 * Idea: un ForwardingJavaFileManager intercetta getJavaFileForOutput e consegna al
 * compilatore un oggetto che scrive in un ByteArrayOutputStream, uno per classe
 * prodotta: una classe con classi annidate o lambda produce piu' voci (Esterna,
 * Esterna$Interna, Esterna$1...), tutte nella mappa. Il loader e' una sottoclasse di
 * ClassLoader il cui findClass cerca nella mappa e chiama defineClass: cosi' anche le
 * classi annidate, richieste dalla JVM al momento del collegamento, si trovano.
 *
 * Errore tipico: tenere solo i byte della classe principale e perdere le annidate:
 * NoClassDefFoundError la prima volta che il codice le tocca.
 */
public class CompilatoreInMemoria {

    private final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final String classPath = System.getProperty("java.class.path");

    public Class<?> compila(String nomeClasse, String sorgente) throws ErroreDiCompilazione {
        Map<String, ByteArrayOutputStream> classi = new HashMap<>();
        DiagnosticCollector<JavaFileObject> diagnostiche = new DiagnosticCollector<>();
        StandardJavaFileManager standard = javac.getStandardFileManager(diagnostiche, null, null);
        JavaFileManager inMemoria = new ForwardingJavaFileManager<>(standard) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location l, String nome, JavaFileObject.Kind k, FileObject s) {
                ByteArrayOutputStream byteClasse = new ByteArrayOutputStream();
                classi.put(nome, byteClasse);
                return new SimpleJavaFileObject(URI.create("bytes:///" + nome.replace('.', '/') + k.extension), k) {
                    @Override public OutputStream openOutputStream() { return byteClasse; }
                };
            }
        };
        JavaFileObject file = new SimpleJavaFileObject(
                URI.create("string:///" + nomeClasse.replace('.', '/') + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean b) { return sorgente; }
        };
        boolean ok = javac.getTask(null, inMemoria, diagnostiche, List.of("-classpath", classPath), null, List.of(file))
                .call();
        if (!ok)
            throw new ErroreDiCompilazione(sorgente, diagnostiche.getDiagnostics());
        ClassLoader loader = new ClassLoader("in-memoria", getClass().getClassLoader()) {
            @Override
            protected Class<?> findClass(String nome) throws ClassNotFoundException {
                ByteArrayOutputStream byteClasse = classi.get(nome);
                if (byteClasse == null)
                    throw new ClassNotFoundException(nome);
                byte[] b = byteClasse.toByteArray();
                return defineClass(nome, b, 0, b.length);
            }
        };
        try {
            return Class.forName(nomeClasse, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T istanzia(String nomeClasse, String sorgente, Class<T> tipo) throws ErroreDiCompilazione {
        Class<?> classe = compila(nomeClasse, sorgente);
        if (!tipo.isAssignableFrom(classe))
            throw new IllegalArgumentException(nomeClasse + " non e' un " + tipo.getSimpleName());
        try {
            return tipo.cast(classe.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
