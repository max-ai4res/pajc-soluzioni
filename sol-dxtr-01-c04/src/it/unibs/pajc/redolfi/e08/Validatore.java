package it.unibs.pajc.redolfi.e08;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import it.unibs.pajc.redolfi.e08.Regole.Intervallo;
import it.unibs.pajc.redolfi.e08.Regole.Lunghezza;
import it.unibs.pajc.redolfi.e08.Regole.NonNullo;

/**
 * Esercizio 4.8: il validatore.
 *
 * Idea: per ogni campo dichiarato (anche privato, con setAccessible) si legge il
 * valore e si controlla ogni annotazione presente. @Intervallo vale per i numeri
 * (Number, dopo l'incartamento dei primitivi), @Lunghezza per le stringhe; un valore
 * null viola @NonNullo ma non le altre regole, altrimenti ogni campo nullo
 * produrrebbe due violazioni. Ogni violazione e' una riga con campo, valore e regola.
 *
 * Errore tipico: controllare @Intervallo su un campo nullo (NullPointerException) o
 * confrontare un Integer con un double senza passare da doubleValue().
 */
public class Validatore {

    public static List<String> valida(Object o) {
        List<String> violazioni = new ArrayList<>();
        for (Field f : o.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Object v;
            try {
                v = f.get(o);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if (f.isAnnotationPresent(NonNullo.class) && v == null)
                violazioni.add(f.getName() + " = null: non puo' essere nullo");
            if (v == null)
                continue;
            Intervallo i = f.getAnnotation(Intervallo.class);
            if (i != null && v instanceof Number n && (n.doubleValue() < i.min() || n.doubleValue() > i.max()))
                violazioni.add(f.getName() + " = " + v + ": fuori da [" + i.min() + ", " + i.max() + "]");
            Lunghezza l = f.getAnnotation(Lunghezza.class);
            if (l != null && v instanceof String s && (s.length() < l.min() || s.length() > l.max()))
                violazioni.add(f.getName() + " = \"" + s + "\": lunghezza fuori da [" + l.min() + ", " + l.max() + "]");
        }
        return violazioni;
    }

    record Ordine(@NonNullo @Lunghezza(min = 3, max = 20) String cliente,
                  @Intervallo(min = 1, max = 100) int quantita,
                  @Intervallo(min = 0.01) double prezzo,
                  @NonNullo String note) { }

    public static void main(String[] args) {
        Object[] prove = { new Ordine("Ada Lovelace", 3, 9.9, "urgente"), new Ordine("Al", 0, 9.9, null),
                new Ordine(null, 500, -1, "") };
        for (Object o : prove) {
            List<String> v = valida(o);
            System.out.println(o + (v.isEmpty() ? " -> valido" : ""));
            v.forEach(r -> System.out.println("   " + r));
        }
    }
}
