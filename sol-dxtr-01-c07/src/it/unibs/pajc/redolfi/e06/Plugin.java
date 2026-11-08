package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Esercizio 7.6: un gestore di plugin riusabile e generico.
 *
 * Idea: un loader per ogni .jar, figlio del loader dell'interfaccia, cosi' un .jar
 * rotto produce una riga in errori() e non ferma gli altri; ServiceLoader si itera
 * con l'iteratore esplicito, perche' un provider rotto lancia
 * ServiceConfigurationError da next() e con il for-each l'errore interromperebbe il
 * ciclo. I loader delle ricariche precedenti si chiudono e si tengono solo in
 * WeakReference: scaricati() conta quante sono state raccolte. Scelta documentata:
 * le classi di .jar diversi non si vedono fra loro.
 *
 * Errore tipico: un solo loader per tutti i .jar (un errore ferma la scoperta a
 * meta'), e tenere i loader vecchi in una lista normale (mai scaricati).
 */
public class Plugin<T> {

    private final Class<T> servizio;
    private final Path cartella;
    private final List<String> errori = new ArrayList<>();
    private final List<URLClassLoader> attuali = new ArrayList<>();
    private final List<WeakReference<URLClassLoader>> vecchi = new ArrayList<>();

    public Plugin(Class<T> servizio, Path cartella) {
        this.servizio = servizio;
        this.cartella = cartella;
    }

    /** Rilegge la cartella: i provider trovati; gli errori per .jar in errori(). */
    public List<T> ricarica() throws IOException {
        for (URLClassLoader l : attuali) {
            l.close();
            vecchi.add(new WeakReference<>(l));
        }
        attuali.clear();
        errori.clear();
        List<T> trovati = new ArrayList<>();
        List<Path> jar;
        try (Stream<Path> file = Files.list(cartella)) {
            jar = file.filter(p -> p.toString().endsWith(".jar")).sorted().toList();
        }
        for (Path p : jar) {
            URLClassLoader loader = new URLClassLoader(p.getFileName().toString(), new URL[] { p.toUri().toURL() },
                    servizio.getClassLoader());
            attuali.add(loader);
            int prima = trovati.size();
            Iterator<T> it = ServiceLoader.load(servizio, loader).iterator();
            while (true) {
                try {
                    if (!it.hasNext())
                        break;
                    trovati.add(it.next());
                } catch (ServiceConfigurationError | LinkageError e) {
                    errori.add(p.getFileName() + ": " + e.getMessage());
                    break;                                    // questo .jar e' andato; i prossimi no
                }
            }
            if (trovati.size() == prima && errori.stream().noneMatch(s -> s.startsWith(p.getFileName().toString())))
                errori.add(p.getFileName() + ": nessun provider (manca META-INF/services/" + servizio.getName() + "?)");
        }
        return trovati;
    }

    public List<String> errori() {
        return List.copyOf(errori);
    }

    /** Quanti loader delle ricariche precedenti il garbage collector ha raccolto. */
    public long scaricati() {
        return vecchi.stream().filter(r -> r.get() == null).count();
    }

    public int vecchi() {
        return vecchi.size();
    }
}
