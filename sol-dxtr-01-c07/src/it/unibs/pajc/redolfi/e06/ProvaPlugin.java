package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Il programma di prova dell'Esercizio 7.6: costruisce in una cartella temporanea tre
 * .jar (uno buono, uno con il file dei servizi chiamato male, uno con una classe il
 * cui costruttore lancia), li carica con Plugin<Saluto>, mostra gli errori isolati,
 * sostituisce il .jar buono con una versione nuova, ricarica e conta i loader
 * scaricati. I .jar si fabbricano compilando una classe al volo con javax.tools (il
 * capitolo 6) e scrivendola con JarOutputStream.
 */
public class ProvaPlugin {

    public static void main(String[] args) throws Exception {
        Path cartella = Files.createTempDirectory("plugin-");
        creaJar(cartella.resolve("a-buono.jar"), "Cordiale", "return \"buongiorno, \" + nome;", true);
        creaJar(cartella.resolve("b-senza-servizi.jar"), "Muto", "return nome;", false);
        creaJar(cartella.resolve("c-rotto.jar"), "Rotto",
                "return nome; } public Rotto() { throw new IllegalStateException(\"non parto\"); ", true);

        Plugin<Saluto> plugin = new Plugin<>(Saluto.class, cartella);
        List<Saluto> saluti = plugin.ricarica();
        System.out.println(saluti.size() + " provider:");
        saluti.forEach(s -> System.out.println("  " + s.getClass().getSimpleName() + ": " + s.saluta("Ada")));
        System.out.println("errori:");
        plugin.errori().forEach(e -> System.out.println("  " + e));

        creaJar(cartella.resolve("a-buono.jar"), "Cordiale", "return \"salve, \" + nome;", true);
        saluti = plugin.ricarica();
        System.out.println("dopo la sostituzione: " + saluti.get(0).saluta("Ada"));
        saluti = null;
        for (int i = 0; i < 20 && plugin.scaricati() < plugin.vecchi(); i++) {
            System.gc();
            Thread.sleep(50);
        }
        System.out.println("loader vecchi: " + plugin.vecchi() + ", scaricati: " + plugin.scaricati());
    }

    /** Un .jar con una classe plugin.<nome> implements Saluto, con o senza il file dei servizi. */
    static void creaJar(Path jar, String nome, String corpo, boolean conServizi) throws IOException {
        String sorgente = "package plugin; public class " + nome + " implements " + Saluto.class.getName()
                + " { public String saluta(String nome) { " + corpo + " } }";
        Path out = Files.createTempDirectory("jar-");
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///plugin/" + nome + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean b) { return sorgente; }
        };
        List<String> opzioni = List.of("-d", out.toString(), "-classpath", System.getProperty("java.class.path"));
        if (!javac.getTask(null, null, null, opzioni, null, List.of(file)).call())
            throw new IllegalStateException("non compila: " + sorgente);
        try (OutputStream f = Files.newOutputStream(jar); JarOutputStream z = new JarOutputStream(f)) {
            z.putNextEntry(new JarEntry("plugin/" + nome + ".class"));
            z.write(Files.readAllBytes(out.resolve("plugin").resolve(nome + ".class")));
            z.closeEntry();
            z.putNextEntry(new JarEntry("META-INF/services/" + (conServizi ? Saluto.class.getName() : "Saluto")));
            z.write(("plugin." + nome + "\n").getBytes());
            z.closeEntry();
        }
    }
}
