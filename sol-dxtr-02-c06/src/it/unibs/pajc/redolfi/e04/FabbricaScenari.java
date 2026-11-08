package it.unibs.pajc.redolfi.e04;

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
        String percorso = "/e04/" + nome + ".txt";
        try (InputStream in = FabbricaScenari.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            return daTesto(new String(in.readAllBytes(), StandardCharsets.UTF_8));
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
