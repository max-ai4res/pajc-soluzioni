package it.unibs.pajc.redolfi.e06;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Esercizio 2.6: la scheda di una classe da riga di comando.
 *
 * Idea: per ogni nome ricevuto, Class.forName e poi le domande all'oggetto Class.
 * La catena delle superclassi e' un ciclo che risale con getSuperclass() finche' non
 * arriva a null; le interfacce si raccolgono con una ricorsione che visita la classe,
 * le sue interfacce (e le interfacce di quelle) e la superclasse, in un LinkedHashSet
 * per evitare i duplicati e mantenere l'ordine di scoperta. Le costanti di un enum
 * arrivano da getEnumConstants(). Un nome sbagliato e' una ClassNotFoundException
 * catturata per quel solo nome.
 *
 * Esempio: java SchedaCompleta java.util.ArrayList java.time.Month 'java.util.Map$Entry' '[I'
 *
 * Errore tipico: getInterfaces() da' solo le interfacce dichiarate direttamente; e
 * getSuperclass() e' null per le interfacce, quindi la catena va scritta con il
 * controllo del null.
 */
public class SchedaCompleta {

    public static void main(String[] args) {
        if (args.length == 0)
            args = new String[] { "java.util.ArrayList", "java.time.Month", "java.util.Map$Entry", "[I" };
        for (String nome : args) {
            try {
                stampa(Class.forName(nome));
            } catch (ClassNotFoundException e) {
                System.out.println("classe non trovata: " + nome);
            }
            System.out.println();
        }
    }

    static void stampa(Class<?> c) {
        System.out.println("nome:         " + c.getName());
        System.out.println("semplice:     " + c.getSimpleName());
        System.out.println("package:      " + c.getPackageName());
        System.out.println("modificatori: " + Modifier.toString(c.getModifiers()));
        System.out.println("tipo:         " + tipo(c));
        System.out.println("superclassi:  " + superclassi(c));
        System.out.println("interfacce:   " + tutteLeInterfacce(c));
        if (c.isRecord()) {
            StringBuilder sb = new StringBuilder();
            for (RecordComponent rc : c.getRecordComponents())
                sb.append(rc.getType().getSimpleName()).append(' ').append(rc.getName()).append("  ");
            System.out.println("componenti:   " + sb);
        }
        if (c.isEnum())
            System.out.println("costanti:     " + List.of(c.getEnumConstants()));
        if (c.isSealed())
            System.out.println("permesse:     " + List.of(c.getPermittedSubclasses()));
    }

    static String tipo(Class<?> c) {
        if (c.isInterface()) return c.isSealed() ? "interfaccia sealed" : "interfaccia";
        if (c.isRecord()) return "record";
        if (c.isEnum()) return "enum";
        if (c.isArray()) return "array di " + c.getComponentType().getSimpleName();
        if (c.isPrimitive()) return "primitivo";
        return c.isSealed() ? "classe sealed" : "classe";
    }

    /** La catena delle superclassi, dalla diretta fino a Object (vuota per le interfacce). */
    static List<String> superclassi(Class<?> c) {
        List<String> catena = new ArrayList<>();
        for (Class<?> s = c.getSuperclass(); s != null; s = s.getSuperclass())
            catena.add(s.getSimpleName());
        return catena;
    }

    /** Tutte le interfacce, dirette e indirette, senza duplicati. */
    static Set<String> tutteLeInterfacce(Class<?> c) {
        Set<String> trovate = new LinkedHashSet<>();
        raccogli(c, trovate);
        return trovate;
    }

    private static void raccogli(Class<?> c, Set<String> trovate) {
        if (c == null)
            return;
        for (Class<?> i : c.getInterfaces()) {
            trovate.add(i.getSimpleName());
            raccogli(i, trovate);                 // le interfacce estese da i
        }
        raccogli(c.getSuperclass(), trovate);     // quelle ereditate
    }
}
