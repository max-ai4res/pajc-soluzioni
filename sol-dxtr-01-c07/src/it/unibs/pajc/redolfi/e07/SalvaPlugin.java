package it.unibs.pajc.redolfi.e07;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/** Scrive un .jar di plugin con i .class di una cartella e il file dei servizi. */
public class SalvaPlugin {

    public static void scrivi(Path jar, Path cartellaClass, List<String> nomiBinari) throws IOException {
        Manifest manifesto = new Manifest();
        manifesto.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifesto.getMainAttributes().putValue("Created-By", "Plot");
        try (OutputStream file = Files.newOutputStream(jar);
             JarOutputStream out = new JarOutputStream(file, manifesto);
             Stream<Path> tutti = Files.walk(cartellaClass)) {
            for (Path p : tutti.filter(p -> p.toString().endsWith(".class")).toList()) {
                String voce = cartellaClass.relativize(p).toString().replace('\\', '/');   // it/unibs/.../F1.class
                out.putNextEntry(new JarEntry(voce));
                out.write(Files.readAllBytes(p));
                out.closeEntry();
            }
            out.putNextEntry(new JarEntry("META-INF/services/" + Funzione.class.getName()));
            out.write(String.join("\n", nomiBinari).concat("\n").getBytes());
            out.closeEntry();
        }
    }
}
