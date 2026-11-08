package it.unibs.pajc.redolfi.e06;

import java.security.CodeSource;
import java.util.function.Supplier;

/**
 * Esercizio 5.6: chi ha caricato chi.
 *
 * Idea: per ogni nome, Class.forName e poi getClassLoader(); null vuol dire bootstrap.
 * La catena dei padri e' un ciclo su getParent() che si ferma a null. Il percorso del
 * .class viene da getProtectionDomain().getCodeSource().getLocation(): e' la cartella
 * o il .jar da cui il loader ha letto la classe, e per le classi del JDK e' null.
 *
 * Errore tipico: l.getParent().getName() senza controllare il null: la prima classe
 * di java.lang ferma il programma con NullPointerException.
 */
public class ChiHaCaricatoChi {

    public static void main(String[] args) throws ClassNotFoundException {
        Supplier<String> lambda = () -> "ciao";
        if (args.length == 0)
            args = new String[] { "java.lang.String", "javax.swing.JFrame", "java.sql.Connection",
                    ChiHaCaricatoChi.class.getName(), lambda.getClass().getName() };
        for (String nome : args) {
            Class<?> c = nome.contains("$$Lambda") ? lambda.getClass() : Class.forName(nome);
            System.out.println(c.getName());
            System.out.print("  loader: ");
            for (ClassLoader l = c.getClassLoader(); ; l = l.getParent()) {
                if (l == null) {
                    System.out.println("bootstrap");
                    break;
                }
                System.out.print(l.getName() + " -> ");
            }
            CodeSource sorgente = c.getProtectionDomain().getCodeSource();
            System.out.println("  da: " + (sorgente == null ? "(il JDK)" : sorgente.getLocation()));
        }
    }
}
