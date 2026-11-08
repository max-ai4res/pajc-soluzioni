package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** I livelli da file: una riga per fila, un carattere per mattone (. v p a). */
public class FabbricaLivelli {

    public static final int QUANTI = 2;
    static final double LARGHEZZA_MATTONE = 1.0, ALTEZZA_MATTONE = 0.4, Y_TOP = Mattoni.ALTEZZA - 0.8;

    public static List<Mattone> daRisorsa(int numero) {
        String percorso = "/e06/livello" + numero + ".txt";
        try (InputStream in = FabbricaLivelli.class.getResourceAsStream(percorso)) {
            if (in == null)
                throw new IllegalArgumentException("manca " + percorso);
            return daTesto(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Mattone> daTesto(String testo) {
        List<Mattone> mattoni = new ArrayList<>();
        String[] righe = testo.split("\n");
        for (int r = 0; r < righe.length; r++)
            for (int c = 0; c < righe[r].length(); c++) {
                Mattone.Tipo tipo = switch (righe[r].charAt(c)) {
                    case 'v' -> Mattone.Tipo.VETRO;
                    case 'p' -> Mattone.Tipo.PIETRA;
                    case 'a' -> Mattone.Tipo.ACCIAIO;
                    case '.' -> null;
                    default -> throw new IllegalArgumentException("riga " + (r + 1) + ": carattere " + righe[r].charAt(c));
                };
                if (tipo != null)
                    mattoni.add(new Mattone(new Ostacolo(c * LARGHEZZA_MATTONE, Y_TOP - (r + 1) * ALTEZZA_MATTONE,
                            LARGHEZZA_MATTONE - 0.05, ALTEZZA_MATTONE - 0.05), tipo));
            }
        return mattoni;
    }
}
