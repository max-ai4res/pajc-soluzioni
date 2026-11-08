package it.unibs.pajc.redolfi.e07;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/**
 * Esercizio 3.7: uguali campo per campo.
 *
 * Idea: stessa classe (getClass() == getClass()), poi per ogni classe della gerarchia
 * fino a Object i campi dichiarati, saltando static (non appartengono all'oggetto) e
 * transient (per scelta: sono lo stato "non essenziale", come nella serializzazione;
 * documentato qui). I primitivi arrivano gia' incartati da Field.get e si confrontano
 * con equals, che per i wrapper e' il confronto per valore; gli oggetti di classi che
 * ridefiniscono equals con equals; quelli di classi nostre che non lo ridefiniscono
 * ricorsivamente, altrimenti due Indirizzo uguali risulterebbero diversi solo perche'
 * sono due oggetti.
 *
 * Il caso in cui stessiCampi ed equals differiscono: una classe che in equals confronta
 * solo una chiave (per esempio il codice fiscale) considera uguali due persone con
 * nomi diversi, stessiCampi no. Ha ragione equals: e' l'autore della classe a decidere
 * che cosa significa "uguale" per i suoi oggetti; stessiCampi e' un confronto
 * strutturale, utile nei test o per rilevare modifiche, non una definizione di
 * identita'.
 *
 * Errore tipico: confrontare i campi con == dopo Field.get: sono Object incartati, e
 * due Integer 1000 con == danno false.
 */
public class StessiCampi {

    public static boolean stessiCampi(Object a, Object b) {
        if (a == b)
            return true;
        if (a == null || b == null || a.getClass() != b.getClass())
            return false;
        for (Class<?> k = a.getClass(); k != null && k != Object.class; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
                    continue;
                f.setAccessible(true);
                try {
                    if (!uguali(f.get(a), f.get(b)))
                        return false;
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return true;
    }

    private static boolean uguali(Object x, Object y) {
        if (x == null || y == null)
            return x == y;
        if (ridefinisceEquals(x.getClass()) || !nostra(x.getClass()))
            return Objects.equals(x, y);
        return stessiCampi(x, y);                   // classe nostra senza equals: per campi
    }

    private static boolean ridefinisceEquals(Class<?> c) {
        try {
            return c.getMethod("equals", Object.class).getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean nostra(Class<?> c) {
        return !c.getPackageName().startsWith("java.");
    }

    // --- classi di prova

    static class Indirizzo {                        // nessun equals: si confronta per campi
        String via; int civico;
        Indirizzo(String via, int civico) { this.via = via; this.civico = civico; }
    }

    static class Persona {
        static int creati;
        String codiceFiscale, nome;
        Indirizzo indirizzo;
        transient int accessi;                      // stato accessorio: ignorato
        Persona(String cf, String nome, Indirizzo ind) { codiceFiscale = cf; this.nome = nome; indirizzo = ind; creati++; }
        @Override public boolean equals(Object o) {   // per l'autore due persone sono uguali se hanno lo stesso CF
            return o instanceof Persona p && p.codiceFiscale.equals(codiceFiscale);
        }
        @Override public int hashCode() { return codiceFiscale.hashCode(); }
    }

    static class Contenitore {                      // nessun equals: confronta Persona con il SUO equals
        Persona p; List<String> tag;
        Contenitore(Persona p, List<String> tag) { this.p = p; this.tag = tag; }
    }

    public static void main(String[] args) {
        Persona a = new Persona("RDLMSM", "Massimo", new Indirizzo("via Roma", 1));
        Persona b = new Persona("RDLMSM", "Massimiliano", new Indirizzo("via Roma", 1));
        Persona c = new Persona("RDLMSM", "Massimo", new Indirizzo("via Roma", 1));
        c.accessi = 99;
        System.out.println("a equals b:      " + a.equals(b));
        System.out.println("stessiCampi a b: " + stessiCampi(a, b) + "   (nomi diversi)");
        System.out.println("stessiCampi a c: " + stessiCampi(a, c) + "   (transient ignorato, Indirizzo per campi)");
        System.out.println("contenitori:     " + stessiCampi(new Contenitore(a, List.of("x")), new Contenitore(b, List.of("x")))
                + "   (Persona confrontata con il suo equals)");
        System.out.println("tipi diversi:    " + stessiCampi(a, "a"));
    }
}
