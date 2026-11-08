package it.unibs.pajc.redolfi.e09;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Esercizio 4.9: Misurato, un proxy che accumula chiamate e tempi per metodo.
 *
 * Idea: il gestore e' una classe (non una lambda) che tiene la mappa delle statistiche,
 * cosi' rapporto(proxy) puo' recuperarlo con Proxy.getInvocationHandler e leggerle.
 * I metodi di Object si trattano a parte: equals confronta con l'oggetto vero
 * "scartando" il proxy dell'argomento, hashCode e toString vanno all'oggetto vero.
 * L'eccezione originale arriva al chiamante perche' il gestore cattura
 * InvocationTargetException e rilancia getCause(): il proxy la propaga tale e quale,
 * sia che sia controllata (se dichiarata dall'interfaccia) sia che non lo sia.
 *
 * Errore tipico: inoltrare equals cosi' com'e' (p.equals(p) risulta falso), e lasciar
 * uscire InvocationTargetException, che il chiamante non si aspetta e che non e' il
 * suo errore.
 */
public final class Misurato implements InvocationHandler {

    private static final class Statistica {
        long chiamate, nanosecondi;
    }

    private final Object vero;
    private final Map<String, Statistica> statistiche = new TreeMap<>();

    private Misurato(Object vero) {
        this.vero = vero;
    }

    @SuppressWarnings("unchecked")
    public static <T> T di(T oggetto, Class<T> interfaccia) {
        return (T) Proxy.newProxyInstance(interfaccia.getClassLoader(), new Class<?>[] { interfaccia }, new Misurato(oggetto));
    }

    @Override
    public Object invoke(Object proxy, Method metodo, Object[] argomenti) throws Throwable {
        if (metodo.getDeclaringClass() == Object.class) {
            return switch (metodo.getName()) {
                case "equals" -> {
                    Object altro = argomenti[0];
                    if (altro != null && Proxy.isProxyClass(altro.getClass())
                            && Proxy.getInvocationHandler(altro) instanceof Misurato m)
                        altro = m.vero;                      // confronta i due oggetti veri
                    yield vero.equals(altro);
                }
                case "hashCode" -> vero.hashCode();
                default -> "Misurato(" + vero + ")";
            };
        }
        long t = System.nanoTime();
        try {
            return metodo.invoke(vero, argomenti);
        } catch (InvocationTargetException e) {
            throw e.getCause();                              // l'eccezione del metodo, non la busta
        } finally {
            Statistica s = statistiche.computeIfAbsent(metodo.getName(), k -> new Statistica());
            s.chiamate++;
            s.nanosecondi += System.nanoTime() - t;
        }
    }

    public static void rapporto(Object proxy) {
        if (!(Proxy.isProxyClass(proxy.getClass()) && Proxy.getInvocationHandler(proxy) instanceof Misurato m))
            throw new IllegalArgumentException("non e' un oggetto misurato");
        System.out.printf("%-12s %9s %12s%n", "metodo", "chiamate", "medio (us)");
        m.statistiche.forEach((nome, s) ->
            System.out.printf("%-12s %9d %12.2f%n", nome, s.chiamate, s.nanosecondi / 1000.0 / s.chiamate));
    }

    public static void main(String[] args) {
        Map<String, Integer> conteggi = Misurato.di(new HashMap<>(), Map.class);
        String testo = "la reflection guarda la classe e la classe guarda la reflection";
        for (String parola : testo.split(" "))
            conteggi.merge(parola, 1, Integer::sum);
        for (int i = 0; i < 1000; i++)
            conteggi.get("la");
        conteggi.containsKey("classe");
        System.out.println(conteggi.size() + " parole; toString: " + conteggi);
        System.out.println("p.equals(p) = " + conteggi.equals(conteggi) + ", hashCode uguale al vero: "
                + (conteggi.hashCode() == new HashMap<>(conteggi).hashCode()));
        try {
            Map<String, Integer> immutabile = Misurato.di(Map.of("a", 1), Map.class);
            immutabile.put("b", 2);
        } catch (UnsupportedOperationException e) {
            System.out.println("eccezione originale ricevuta: " + e.getClass().getSimpleName());
        }
        rapporto(conteggi);
    }
}
