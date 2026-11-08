package it.unibs.pajc.redolfi.e07;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/** Le funzioni che arrivano dai .jar di una cartella: un loader nuovo a ogni ricarica. */
public class Plugin {

    private final Path cartella;
    private URLClassLoader loader;                 // quello dell'ultima ricarica, da chiudere

    public Plugin(Path cartella) {
        this.cartella = cartella;
    }

    /** Legge tutti i .jar della cartella e restituisce le Funzione che dichiarano. */
    public List<Voce> ricarica() throws IOException {
        if (loader != null)
            loader.close();                        // il vecchio: le sue classi restano a chi le usa ancora
        URL[] jar;
        try (Stream<Path> file = Files.list(cartella)) {
            jar = file.filter(p -> p.toString().endsWith(".jar")).map(Plugin::url).toArray(URL[]::new);
        }
        loader = new URLClassLoader("plugin", jar, Funzione.class.getClassLoader());
        List<Voce> voci = new ArrayList<>();
        for (Funzione f : ServiceLoader.load(Funzione.class, loader)) {
            Plot p = f.getClass().getAnnotation(Plot.class);
            voci.add(p == null ? new Voce(f.getClass().getSimpleName(), f, -10, 10)
                               : new Voce(p.nome(), f, p.xMin(), p.xMax()));
        }
        return voci;
    }

    private static URL url(Path p) {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(p.toString(), e);
        }
    }
}
