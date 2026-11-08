package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * La fabbrica degli scenari: legge una descrizione di testo e costruisce il mondo.
 * Una riga per elemento:
 *   mondo 8 6                      larghezza e altezza in metri
 *   gravita 9.81
 *   ostacolo 1.0 3.0 2.5 0.2       x y larghezza altezza
 *   pallina 2 5 0.25 gomma         x y raggio materiale
 * Le righe vuote e quelle che iniziano con # si saltano.
 */
public class FabbricaScenari {

    public static Mondo daTesto(String testo) {
        Mondo mondo = null;
        for (String riga : testo.split("\n")) {
            riga = riga.strip();
            if (riga.isEmpty() || riga.startsWith("#"))
                continue;
            String[] c = riga.split("\\s+");
            switch (c[0]) {
                case "mondo" -> mondo = new Mondo(d(c[1]), d(c[2]));
                case "gravita" -> richiesto(mondo).setGravita(d(c[1]));
                case "ostacolo" -> richiesto(mondo).aggiungi(
                        new Ostacolo(d(c[1]), d(c[2]), d(c[3]), d(c[4])));
                case "pallina" -> richiesto(mondo).lascia(
                        new Vec2(d(c[1]), d(c[2])), d(c[3]),
                        Materiale.daNome(c[4]));
                default -> throw new IllegalArgumentException(
                        "riga non capita: " + riga);
            }
        }
        return richiesto(mondo);
    }

    /** Uno scenario fra le risorse del programma: /scenari/nome.txt. */
    public static Mondo daRisorsa(String nome) {
        String percorso = "/e08/" + nome + ".txt";
        try (InputStream in = FabbricaScenari.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            return daTesto(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** La fabbrica al contrario: il mondo com'e' adesso, nel formato che daTesto legge. */
    public static String aTesto(Mondo m) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(java.util.Locale.ROOT, "mondo %.3f %.3f%n", m.larghezza(), m.altezza()));
        sb.append(String.format(java.util.Locale.ROOT, "gravita %.3f%n", m.gravita()));
        for (Ostacolo o : m.ostacoli())
            sb.append(String.format(java.util.Locale.ROOT, "ostacolo %.3f %.3f %.3f %.3f%n", o.x(), o.y(), o.larghezza(), o.altezza()));
        for (Pallina pal : m.palline())
            sb.append(String.format(java.util.Locale.ROOT, "pallina %.3f %.3f %.3f %s%n", pal.posizione().x(), pal.posizione().y(),
                    pal.raggio(), nome(pal.materiale())));
        return sb.toString();
    }

    static String nome(Materiale m) {
        return switch (m) {
            case Gomma g -> "gomma";
            case Piombo pb -> "piombo";
            case Bolla b -> "bolla";
        };
    }

    /** Uno scenario da un file scelto dall'utente, fuori dalle risorse. */
    public static Mondo daFile(java.nio.file.Path file) {
        try {
            return daTesto(java.nio.file.Files.readString(file));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static double d(String s) {
        return Double.parseDouble(s);
    }

    private static Mondo richiesto(Mondo m) {
        if (m == null)
            throw new IllegalArgumentException("la prima riga deve essere 'mondo larghezza altezza'");
        return m;
    }
}
