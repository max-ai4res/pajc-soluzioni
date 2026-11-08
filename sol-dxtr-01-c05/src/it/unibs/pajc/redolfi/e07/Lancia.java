package it.unibs.pajc.redolfi.e07;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Esercizio 5.7: un lanciatore.
 *
 * Idea: URLClassLoader sulla cartella (o sul .jar) ricevuta, Class.forName con quel
 * loader, getMethod("main", String[].class) e un controllo che sia static; poi invoke
 * con null come bersaglio e, attenzione, l'array degli argomenti passato come UNICO
 * argomento, quindi incartato: invoke(null, (Object) resto). Un'eccezione del main
 * arriva incartata in InvocationTargetException: si stampa la causa, con la sua
 * traccia, come farebbe la JVM.
 *
 * Esempio: java Lancia res/e07 saluti.Saluta Ada Bob  (res/e07 contiene saluti/Saluta.class,
 * compilato da res/e07/Saluta.java con javac -d res/e07 res/e07/Saluta.java)
 *
 * Errore tipico: main.invoke(null, resto) senza il cast: con due argomenti "wrong
 * number of arguments", con uno "argument type mismatch".
 */
public class Lancia {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("uso: Lancia <cartella o .jar> <classe> [argomenti...]");
            return;
        }
        URL dove = Path.of(args[0]).toUri().toURL();
        String[] resto = Arrays.copyOfRange(args, 2, args.length);
        try (URLClassLoader loader = new URLClassLoader("lanciatore", new URL[] { dove }, Lancia.class.getClassLoader())) {
            Class<?> classe = Class.forName(args[1], true, loader);
            Method main = classe.getMethod("main", String[].class);
            if (!Modifier.isStatic(main.getModifiers())) {
                System.out.println(args[1] + ": main non e' static");
                return;
            }
            main.invoke(null, (Object) resto);
        } catch (ClassNotFoundException e) {
            System.out.println("classe non trovata in " + args[0] + ": " + args[1]);
        } catch (NoSuchMethodException e) {
            System.out.println(args[1] + " non ha un main(String[])");
        } catch (InvocationTargetException e) {
            System.out.println("il main ha lanciato un'eccezione:");
            e.getCause().printStackTrace(System.out);          // la causa, non la busta
        }
    }
}
