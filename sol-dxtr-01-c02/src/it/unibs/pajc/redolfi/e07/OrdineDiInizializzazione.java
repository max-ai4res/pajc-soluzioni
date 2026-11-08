package it.unibs.pajc.redolfi.e07;

/**
 * Esercizio 2.7: l'ordine di inizializzazione di tre classi.
 *
 * A ha un campo statico di tipo B creato con new B(): inizializzare A richiede di
 * creare un B, quindi di inizializzare B prima che il blocco statico di A sia finito.
 * B prende da C una costante static final int: e' un valore copiato dal compilatore,
 * e C non viene mai inizializzata.
 *
 * Previsione per "new A()": [B] (dentro l'inizializzazione di A, prima del blocco di A
 * perche' il campo statico viene prima nel sorgente), poi [A]; C non compare.
 * Previsione per forName("...A", false, loader) + A.class.getSuperclass(): niente,
 * perche' ne' il caricamento ne' getSuperclass inizializzano.
 *
 * Errore tipico: aspettarsi [C] perche' "B usa C": una costante compile-time non
 * tocca la classe che la dichiara.
 */
public class OrdineDiInizializzazione {

    static class A {
        static B b = new B();
        static { System.out.println("[A]"); }
    }

    static class B {
        static final int LIMITE = C.MAX;
        static { System.out.println("[B] limite = " + LIMITE); }
    }

    static class C {
        static final int MAX = 100;
        static { System.out.println("[C]"); }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length > 0 && args[0].equals("solo-carica")) {
            System.out.println("carico A senza inizializzare:");
            Class<?> a = Class.forName(A.class.getName(), false, OrdineDiInizializzazione.class.getClassLoader());
            System.out.println("superclasse di A: " + a.getSuperclass().getSimpleName());
            System.out.println("fine (nessun blocco statico eseguito)");
        } else {
            System.out.println("new A():");
            new A();
            System.out.println("fine");
        }
    }
}
