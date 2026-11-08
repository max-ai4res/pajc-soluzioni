package it.unibs.pajc.redolfi.e07;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Esercizio 4.7: il mini-JUnit completo.
 *
 * Idea: come il runner della dispensa, piu': i @Dopo in un finally, cosi' girano anche
 * se il test fallisce; il timeout con un thread a parte e join(ms): se il thread e'
 * ancora vivo dopo il limite il test e' fallito e il thread viene interrotto;
 * l'eccezione del test e' trasportata dal thread al runner in un campo; il tempo di
 * ogni test con System.nanoTime; System.exit(1) alla fine se qualcosa e' fallito, cosi'
 * uno script puo' accorgersene. La classe arriva dalla riga di comando.
 *
 * Errore tipico: eseguire il test nel thread principale e "misurare" il timeout dopo:
 * un test che non termina blocca tutto. E dimenticare che un'eccezione lanciata in un
 * altro thread non arriva al chiamante: va raccolta a mano.
 */
public class MiniJUnit {

    private static final class Esito {
        Throwable errore;
    }

    public static int esegui(Class<?> classe) throws ReflectiveOperationException, InterruptedException {
        List<Method> test = new ArrayList<>(), prima = new ArrayList<>(), dopo = new ArrayList<>();
        for (Method m : classe.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) test.add(m);
            if (m.isAnnotationPresent(Prima.class)) prima.add(m);
            if (m.isAnnotationPresent(Dopo.class)) dopo.add(m);
        }
        test.sort(Comparator.comparing(Method::getName));
        int falliti = 0;
        long totale = System.nanoTime();
        for (Method m : test) {
            Object istanza = classe.getDeclaredConstructor().newInstance();
            Test t = m.getAnnotation(Test.class);
            long inizio = System.nanoTime();
            String esito;
            try {
                for (Method p : prima) p.invoke(istanza);
                Throwable lanciata = lancia(m, istanza, t.timeout());
                if (lanciata == null)
                    esito = t.atteso() == Test.Nessuna.class ? "ok" : "FALLITO: attesa " + t.atteso().getSimpleName();
                else if (t.atteso().isInstance(lanciata))
                    esito = "ok (" + lanciata.getClass().getSimpleName() + ")";
                else
                    esito = "FALLITO: " + lanciata;
            } finally {
                for (Method d : dopo) d.invoke(istanza);
            }
            if (!esito.startsWith("ok")) falliti++;
            System.out.printf("  %-28s %-45s %6.1f ms%n", m.getName(), esito, (System.nanoTime() - inizio) / 1e6);
        }
        System.out.printf("%s: %d/%d passati in %.1f ms%n", classe.getSimpleName(), test.size() - falliti, test.size(),
                (System.nanoTime() - totale) / 1e6);
        return falliti;
    }

    /** Chiama il test, con o senza limite di tempo; restituisce l'eccezione lanciata o null. */
    private static Throwable lancia(Method m, Object istanza, long timeout) throws InterruptedException {
        Esito esito = new Esito();
        Runnable corpo = () -> {
            try {
                m.invoke(istanza);
            } catch (InvocationTargetException e) {
                esito.errore = e.getCause();
            } catch (IllegalAccessException e) {
                esito.errore = e;
            }
        };
        if (timeout <= 0) {
            corpo.run();
            return esito.errore;
        }
        Thread thread = new Thread(corpo, "test-" + m.getName());
        thread.setDaemon(true);
        thread.start();
        thread.join(timeout);
        if (thread.isAlive()) {
            thread.interrupt();
            return new AssertionError("timeout di " + timeout + " ms superato");
        }
        return esito.errore;
    }

    public static void main(String[] args) throws Exception {
        String nome = args.length > 0 ? args[0] : ProvaStringhe.class.getName();
        int falliti = esegui(Class.forName(nome));
        if (falliti > 0)
            System.exit(1);
    }
}
