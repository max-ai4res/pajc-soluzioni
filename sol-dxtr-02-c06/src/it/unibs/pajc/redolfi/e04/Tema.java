package it.unibs.pajc.redolfi.e04;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Un tema: immutabile e condiviso (Flyweight), uno per nome, letto da un file di proprieta'. */
public record Tema(String nome, Color sfondo, Color pavimento, Color gomma, Color gommaFerma, Color piombo, Color bolla,
        float bordo, boolean schegge) {

    private static final Map<String, Tema> CACHE = new HashMap<>();

    /** La fabbrica: lo stesso oggetto per lo stesso nome. */
    public static synchronized Tema daNome(String nome) {
        return CACHE.computeIfAbsent(nome, Tema::carica);
    }

    private static Tema carica(String nome) {
        String percorso = "/e04/tema-" + nome + ".properties";
        try (InputStream in = Tema.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            Properties p = new Properties();
            p.load(in);
            return new Tema(nome, colore(p, "sfondo"), colore(p, "pavimento"), colore(p, "gomma"), colore(p, "gomma.ferma"),
                    colore(p, "piombo"), colore(p, "bolla"), Float.parseFloat(p.getProperty("bordo", "0")),
                    Boolean.parseBoolean(p.getProperty("schegge", "true")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Color colore(Properties p, String chiave) {
        return new Color(Integer.parseInt(p.getProperty(chiave, "FF00FF"), 16));
    }
}
