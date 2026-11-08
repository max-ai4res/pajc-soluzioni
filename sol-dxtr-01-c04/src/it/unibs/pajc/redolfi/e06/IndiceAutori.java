package it.unibs.pajc.redolfi.e06;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Esercizio 4.6: l'indice degli autori.
 *
 * Idea: per ogni classe (dal nome) si leggono le @Autore con getAnnotationsByType, che
 * restituisce l'array sia quando l'annotazione e' una sola sia quando il compilatore
 * le ha impacchettate nel contenitore @Autori; lo stesso per ogni metodo dichiarato.
 * Le voci finiscono in una TreeMap autore -> lista di contributi, che tiene gli
 * autori in ordine alfabetico.
 *
 * Errore tipico: getAnnotation(Autore.class) su un elemento con due @Autore: null,
 * perche' li' c'e' solo @Autori.
 */
public class IndiceAutori {

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length == 0)
            args = new String[] { Esempi.Parser.class.getName(), Esempi.Stampante.class.getName(),
                    Esempi.Anonima.class.getName() };
        Map<String, List<String>> indice = new TreeMap<>();
        for (String nome : args) {
            Class<?> c = Class.forName(nome);
            for (Autore a : c.getAnnotationsByType(Autore.class))
                indice.computeIfAbsent(a.nome(), k -> new ArrayList<>()).add(c.getSimpleName() + " (" + a.anno() + ")");
            for (Method m : c.getDeclaredMethods())
                for (Autore a : m.getAnnotationsByType(Autore.class))
                    indice.computeIfAbsent(a.nome(), k -> new ArrayList<>())
                          .add(c.getSimpleName() + "." + m.getName() + "() (" + a.anno() + ")");
        }
        indice.forEach((autore, voci) -> System.out.println(autore + ": " + String.join(", ", voci)));
        // la trappola, mostrata
        System.out.println("getAnnotation su Parser: " + Esempi.Parser.class.getAnnotation(Autore.class));
        System.out.println("getAnnotation(Autori) su Parser: " + Esempi.Parser.class.getAnnotation(Autori.class));
    }
}
