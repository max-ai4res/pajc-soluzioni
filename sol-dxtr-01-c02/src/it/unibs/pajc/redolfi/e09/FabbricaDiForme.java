package it.unibs.pajc.redolfi.e09;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Esercizio 2.9: la fabbrica di forme da un file di testo.
 *
 * Idea: ogni riga e' "NomeSemplice misura misura...". Il nome si completa con il
 * package di questa classe (le forme stanno qui accanto) e si risolve con forName; si
 * controlla che sia uno Shape2d con isAssignableFrom, si cerca con getConstructor il
 * costruttore con tanti double.class quante sono le misure, e lo si chiama. Tutti i
 * modi in cui una riga puo' essere sbagliata (classe inesistente, non una forma,
 * costruttore con un numero diverso di misure, numero non valido) finiscono in un
 * messaggio e la riga si salta.
 *
 * Errore tipico: fare il cast a Shape2d senza isAssignableFrom (ClassCastException con
 * una riga "String 3"), o cercare il costruttore con Double.class invece di
 * double.class: NoSuchMethodException, perche' per la reflection sono tipi diversi.
 */
public class FabbricaDiForme {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "res/e09/scena.txt");
        List<Shape2d> forme = new ArrayList<>();
        int numero = 0;
        for (String riga : Files.readAllLines(file)) {
            numero++;
            riga = riga.strip();
            if (riga.isEmpty() || riga.startsWith("#"))
                continue;
            try {
                forme.add(crea(riga));
            } catch (IllegalArgumentException e) {
                System.out.println("riga " + numero + " saltata: " + e.getMessage());
            }
        }
        double totale = forme.stream().mapToDouble(Shape2d::area).sum();
        System.out.printf("%d forme, area totale %.2f%n", forme.size(), totale);
        forme.stream().max(Comparator.comparingDouble(Shape2d::area))
                .ifPresent(f -> System.out.println("la piu' grande: " + f));
    }

    static Shape2d crea(String riga) {
        String[] campi = riga.split("\\s+");
        String nome = FabbricaDiForme.class.getPackageName() + "." + campi[0];
        Class<?> classe;
        try {
            classe = Class.forName(nome);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("forma sconosciuta: " + campi[0]);
        }
        if (!Shape2d.class.isAssignableFrom(classe))
            throw new IllegalArgumentException(campi[0] + " non e' una forma");
        int n = campi.length - 1;
        Class<?>[] firma = new Class<?>[n];
        Arrays.fill(firma, double.class);
        Object[] misure = new Object[n];
        try {
            for (int i = 0; i < n; i++)
                misure[i] = Double.parseDouble(campi[i + 1]);
            Constructor<?> costruttore = classe.getConstructor(firma);
            return (Shape2d) costruttore.newInstance(misure);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("misura non valida in: " + riga);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(campi[0] + " non si costruisce con " + n + " misure");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("il costruttore ha fallito: " + e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("impossibile creare " + campi[0] + ": " + e);
        }
    }
}
