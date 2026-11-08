package it.unibs.pajc.redolfi.e02;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Esercizio 6.2, la fabbrica di forme.
 *
 * Idea: le forme sono decorazioni della vista, in coordinate del mondo (metri, y in su);
 * la fabbrica le legge da un file fra le risorse e restituisce Shape di java.awt.geom.
 * Sta dalla parte della vista: il modello tiene solo il nome del file. Gli errori
 * portano il numero di riga.
 *
 * Errore tipico: dare al Mondo una lista di Shape "per comodita'": il modello importerebbe
 * AWT per una decorazione.
 */
public class FabbricaForme {

    public static List<Shape> daRisorsa(String nome) {
        String percorso = "/e02/" + nome + ".txt";
        try (InputStream in = FabbricaForme.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            return daTesto(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Shape> daTesto(String testo) {
        List<Shape> forme = new ArrayList<>();
        String[] righe = testo.split("\n");
        for (int n = 0; n < righe.length; n++) {
            String riga = righe[n].strip();
            if (riga.isEmpty() || riga.startsWith("#"))
                continue;
            try {
                String[] c = riga.split("\\s+");
                switch (c[0]) {
                    case "cerchio" -> forme.add(new Ellipse2D.Double(d(c[1]) - d(c[3]), d(c[2]) - d(c[3]), 2 * d(c[3]), 2 * d(c[3])));
                    case "rettangolo" -> forme.add(new Rectangle2D.Double(d(c[1]), d(c[2]), d(c[3]), d(c[4])));
                    case "poligono" -> {
                        if (c.length < 7 || c.length % 2 == 0)
                            throw new IllegalArgumentException("un poligono vuole almeno tre coppie x y");
                        Path2D.Double p = new Path2D.Double();
                        p.moveTo(d(c[1]), d(c[2]));
                        for (int i = 3; i < c.length; i += 2)
                            p.lineTo(d(c[i]), d(c[i + 1]));
                        p.closePath();
                        forme.add(p);
                    }
                    default -> throw new IllegalArgumentException("forma sconosciuta: " + c[0]);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("riga " + (n + 1) + ": " + e.getMessage(), e);
            }
        }
        return forme;
    }

    private static double d(String s) {
        return Double.parseDouble(s);
    }
}
