package it.unibs.pajc.redolfi.e07;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Le funzioni che il Plot conosce, con il nome e l'intervallo letti dall'annotazione @Plot. */
public class Catalogo {

    private final List<Voce> voci = new ArrayList<>();

    public Catalogo() {
        aggiungiMetodiDi(Math.class);
        aggiungiMetodiDi(it.unibs.pajc.redolfi.e07.funzioni.Polinomi.class);
        for (String nome : new String[] { "Gaussiana", "Sinc", "Onda" })
            aggiungiClasse("it.unibs.pajc.redolfi.e07.funzioni." + nome);
    }

    /** I metodi "static double f(double)": tutti se la classe non usa @Plot, solo gli annotati altrimenti. */
    public void aggiungiMetodiDi(Class<?> classe) {
        List<Method> candidati = Arrays.stream(classe.getMethods())
              .filter(m -> Modifier.isStatic(m.getModifiers()))
              .filter(m -> m.getReturnType() == double.class)
              .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == double.class)
              .sorted(Comparator.comparing(Method::getName))
              .toList();
        boolean annotata = candidati.stream().anyMatch(m -> m.isAnnotationPresent(Plot.class));
        for (Method m : candidati) {
            Plot p = m.getAnnotation(Plot.class);
            if (annotata && p == null)
                continue;
            voci.add(p == null ? new Voce(m.getName() + "(x)", incarta(m), -10, 10)
                               : new Voce(p.nome(), incarta(m), p.xMin(), p.xMax()));
        }
    }

    /** Una classe Funzione dal suo nome; il nome nel men\`u e l'intervallo da @Plot, se c'\`e. */
    public void aggiungiClasse(String nomeClasse) {
        try {
            Class<?> classe = Class.forName(nomeClasse);
            if (!Funzione.class.isAssignableFrom(classe))
                throw new IllegalArgumentException(nomeClasse + " non implementa Funzione");
            Funzione fn = (Funzione) classe.getDeclaredConstructor().newInstance();
            Plot p = classe.getAnnotation(Plot.class);
            voci.add(p == null ? new Voce(classe.getSimpleName(), fn, -10, 10)
                               : new Voce(p.nome(), fn, p.xMin(), p.xMax()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("impossibile usare " + nomeClasse, e);
        }
    }

    private static Funzione incarta(Method m) {
        return x -> {
            try {
                return (double) m.invoke(null, x);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("errore in " + m.getName(), e);
            }
        };
    }

    public List<Voce> voci() {
        return voci;
    }
}
