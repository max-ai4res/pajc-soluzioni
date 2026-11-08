package it.unibs.pajc.redolfi.e03;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Esercizio 7.3: il runner che trova le classi da solo.
 *
 * Idea: si percorre una cartella di .class (o le voci di un .jar) e ogni percorso
 * diventa un nome binario (radice tolta, separatori -> punti, ".class" tolto, il $
 * resta). Ogni classe si carica con forName(nome, false, loader): senza
 * inizializzarla, cosi' un blocco statico che fa danni non parte solo perche' la
 * classe e' stata guardata. Se ha almeno un metodo @Test, si esegue con il runner
 * del capitolo 4 (qui copiato). Le classi che non si lasciano caricare (dipendenze
 * mancanti: NoClassDefFoundError e' un Error, non un'Exception) si segnalano e si
 * saltano.
 *
 * Uso: java RunnerCartella bin   (o un .jar); con la cartella bin di questo progetto
 * trova e esegue ProvaStack qui sotto.
 *
 * Errore tipico: sostituire anche il $ con un punto nel nome ricavato dal percorso,
 * o costruire il nome con "/" fisso invece del separatore della piattaforma.
 */
public class RunnerCartella {

    public static void main(String[] args) throws IOException {
        Path radice = Path.of(args.length > 0 ? args[0] : "bin");
        List<String> nomi = radice.toString().endsWith(".jar") ? nomiDalJar(radice) : nomiDallaCartella(radice);
        URLClassLoader loader = new URLClassLoader(new URL[] { radice.toUri().toURL() },
                RunnerCartella.class.getClassLoader());
        int classi = 0, passatiTot = 0, testTot = 0;
        for (String nome : nomi) {
            Class<?> c;
            try {
                c = Class.forName(nome, false, loader);
            } catch (ClassNotFoundException | LinkageError e) {
                System.out.println("non caricabile: " + nome + " (" + e + ")");
                continue;
            }
            boolean haTest = false;
            for (Method m : c.getDeclaredMethods())
                haTest |= m.isAnnotationPresent(Test.class);
            if (!haTest)
                continue;
            try {
                int[] esito = esegui(c);
                classi++;
                passatiTot += esito[0];
                testTot += esito[1];
            } catch (ReflectiveOperationException e) {
                System.out.println("impossibile eseguire " + nome + ": " + e);
            }
        }
        System.out.printf("%ntotale: %d classi di test, %d/%d test passati%n", classi, passatiTot, testTot);
    }

    static List<String> nomiDallaCartella(Path radice) throws IOException {
        try (Stream<Path> file = Files.walk(radice)) {
            return file.filter(p -> p.toString().endsWith(".class"))
                    .map(p -> radice.relativize(p).toString())
                    .map(s -> s.substring(0, s.length() - ".class".length()).replace(radice.getFileSystem().getSeparator(), "."))
                    .sorted().toList();
        }
    }

    static List<String> nomiDalJar(Path jar) throws IOException {
        try (JarFile f = new JarFile(jar.toFile())) {
            return f.stream().map(e -> e.getName()).filter(n -> n.endsWith(".class"))
                    .map(n -> n.substring(0, n.length() - ".class".length()).replace('/', '.'))
                    .sorted().toList();
        }
    }

    /** Il runner del capitolo 4: restituisce {passati, totale}. */
    static int[] esegui(Class<?> classe) throws ReflectiveOperationException {
        List<Method> test = new ArrayList<>(), prima = new ArrayList<>();
        for (Method m : classe.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) test.add(m);
            if (m.isAnnotationPresent(Prima.class)) prima.add(m);
        }
        test.sort(Comparator.comparing(Method::getName));
        int passati = 0;
        System.out.println(classe.getName() + ":");
        for (Method m : test) {
            Object istanza = classe.getDeclaredConstructor().newInstance();
            for (Method p : prima) p.invoke(istanza);
            Class<? extends Throwable> atteso = m.getAnnotation(Test.class).atteso();
            String esito;
            try {
                m.invoke(istanza);
                esito = atteso == Test.Nessuna.class ? "ok" : "FALLITO: attesa " + atteso.getSimpleName();
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                esito = atteso.isInstance(t) ? "ok (" + t.getClass().getSimpleName() + ")" : "FALLITO: " + t;
            }
            if (esito.startsWith("ok")) passati++;
            System.out.printf("  %-24s %s%n", m.getName(), esito);
        }
        return new int[] { passati, test.size() };
    }
}
