package it.unibs.pajc.redolfi.e02;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Esercizio 7.2: la scheda del bimondo.
 *
 * Idea: da Shape2d.class, getPermittedSubclasses() da' le classi senza nominarle;
 * per ciascuna i componenti del record e i costruttori. La mappa nome semplice ->
 * classe permessa serve a costruire le forme del file: i tipi del costruttore sono
 * quelli dei componenti, nell'ordine. area e perimeter si chiamano per reflection con
 * i Method presi UNA volta da Shape2d (sono metodi d'interfaccia: invoke su
 * un'istanza qualunque sceglie l'implementazione giusta, come una chiamata normale).
 *
 * Errore tipico: getDeclaredConstructor() senza tipi su un record
 * (NoSuchMethodException), e il costruttore compatto che lancia (triangolo
 * impossibile) visto come InvocationTargetException senza guardare la causa.
 */
public class SchedaBimondo {

    public static void main(String[] args) throws Exception {
        Map<String, Class<?>> permesse = Arrays.stream(Shape2d.class.getPermittedSubclasses())
                .collect(Collectors.toMap(Class::getSimpleName, c -> c));
        System.out.println("gerarchia di " + Shape2d.class.getSimpleName() + ":");
        for (Class<?> c : Shape2d.class.getPermittedSubclasses()) {
            System.out.print("  " + c.getSimpleName() + (c.isRecord() ? " (record)" : "") + ": componenti");
            for (RecordComponent rc : c.getRecordComponents())
                System.out.print(" " + rc.getType().getSimpleName() + " " + rc.getName());
            System.out.println("; costruttori: " + Arrays.toString(c.getDeclaredConstructors()).replace(c.getPackageName() + ".", ""));
        }

        Method area = Shape2d.class.getMethod("area"), perimeter = Shape2d.class.getMethod("perimeter");
        Path file = Path.of(args.length > 0 ? args[0] : "res/e02/forme.txt");
        List<Object> forme = new ArrayList<>();
        int n = 0;
        for (String riga : Files.readAllLines(file)) {
            n++;
            if (riga.isBlank() || riga.startsWith("#"))
                continue;
            try {
                forme.add(crea(riga.strip().split("\\s+"), permesse));
            } catch (IllegalArgumentException e) {
                System.out.println("riga " + n + " saltata: " + e.getMessage());
            }
        }
        System.out.printf("%n%-32s %10s %10s%n", "forma", "area", "perimetro");
        double totArea = 0, totPer = 0;
        for (Object f : forme) {
            double a = (double) area.invoke(f), p = (double) perimeter.invoke(f);
            totArea += a;
            totPer += p;
            System.out.printf("%-32s %10.3f %10.3f%n", f, a, p);
        }
        System.out.printf("%-32s %10.3f %10.3f%n", "totale (" + forme.size() + " forme)", totArea, totPer);
    }

    static Object crea(String[] campi, Map<String, Class<?>> permesse) {
        Class<?> c = permesse.get(campi[0]);
        if (c == null)
            throw new IllegalArgumentException("forma sconosciuta: " + campi[0]);
        RecordComponent[] componenti = c.getRecordComponents();
        if (componenti.length != campi.length - 1)
            throw new IllegalArgumentException(campi[0] + " vuole " + componenti.length + " misure");
        Class<?>[] tipi = Arrays.stream(componenti).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Object[] valori = new Object[tipi.length];
        try {
            for (int i = 0; i < tipi.length; i++)
                valori[i] = Double.parseDouble(campi[i + 1]);
            Constructor<?> costruttore = c.getDeclaredConstructor(tipi);
            return costruttore.newInstance(valori);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("misura non valida: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getCause().getMessage());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
}
