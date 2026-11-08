package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Esercizio 5.8: un'applicazione a comandi con i plugin in plugin/.
 *
 * Idea: ricarica() chiude il loader precedente, ne crea uno nuovo su tutti i .jar
 * della cartella con il loader di Comando come padre, e con ServiceLoader raccoglie i
 * comandi in una mappa per nome. "aiuto" e "ricarica" sono comandi interni. Un
 * provider rotto (ServiceConfigurationError) si segnala e non ferma gli altri.
 * I comandi di prova stanno nel progetto sol-dxtr-01-c05-plugin: esportato come
 * plugin/comandi.jar (gia' incluso) con il file META-INF/services.
 *
 * Errore tipico: mettere una copia di Comando.java nel progetto del plugin e creare il
 * loader con padre null: "Comando cannot be cast to Comando".
 */
public class AppComandi {

    private final Path cartella;
    private final TreeMap<String, Comando> comandi = new TreeMap<>();
    private URLClassLoader loader;

    AppComandi(Path cartella) {
        this.cartella = cartella;
    }

    void ricarica() throws IOException {
        if (loader != null)
            loader.close();
        URL[] jar;
        try (Stream<Path> file = Files.list(cartella)) {
            jar = file.filter(p -> p.toString().endsWith(".jar")).map(AppComandi::url).toArray(URL[]::new);
        }
        loader = new URLClassLoader("plugin", jar, Comando.class.getClassLoader());
        comandi.clear();
        var iteratore = ServiceLoader.load(Comando.class, loader).iterator();
        while (true) {
            try {
                if (!iteratore.hasNext())
                    break;
                Comando c = iteratore.next();
                comandi.put(c.nome(), c);
            } catch (ServiceConfigurationError e) {
                System.out.println("plugin saltato: " + e.getMessage());
            }
        }
        System.out.println(comandi.size() + " comandi da " + jar.length + " jar in " + cartella);
    }

    private static URL url(Path p) {
        try {
            return p.toUri().toURL();
        } catch (IOException e) {
            throw new IllegalArgumentException(p.toString(), e);
        }
    }

    public static void main(String[] args) throws IOException {
        AppComandi app = new AppComandi(Path.of(args.length > 0 ? args[0] : "plugin"));
        app.ricarica();
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("> ");
            while (in.hasNextLine()) {
                List<String> parole = new ArrayList<>(Arrays.asList(in.nextLine().strip().split("\\s+")));
                String nome = parole.remove(0);
                switch (nome) {
                    case "" -> { }
                    case "fine" -> { return; }
                    case "ricarica" -> app.ricarica();
                    case "aiuto" -> {
                        System.out.println("  aiuto, ricarica, fine");
                        app.comandi.values().forEach(c -> System.out.println("  " + c.nome() + ": " + c.aiuto()));
                    }
                    default -> {
                        Comando c = app.comandi.get(nome);
                        if (c == null)
                            System.out.println("comando sconosciuto: " + nome + " (aiuto per l'elenco)");
                        else
                            c.esegui(parole);
                    }
                }
                System.out.print("> ");
            }
        }
    }
}
