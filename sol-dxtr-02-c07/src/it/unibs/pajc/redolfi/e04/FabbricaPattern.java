package it.unibs.pajc.redolfi.e04;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** I pattern celebri, da file di testo fra le risorse: righe di . e O. */
public class FabbricaPattern {

    public static final String[] NOMI = { "aliante", "navicella", "cannone" };

    public static List<String> daRisorsa(String nome) {
        String percorso = "/e04/" + nome + ".txt";
        try (InputStream in = FabbricaPattern.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().filter(s -> !s.isBlank()).toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
