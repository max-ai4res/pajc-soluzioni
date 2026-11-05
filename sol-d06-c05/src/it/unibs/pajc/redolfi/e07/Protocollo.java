package it.unibs.pajc.redolfi.e07;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Il protocollo della calcolatrice remota, dal lato del client: la tabella dei comandi
 * che il server conosce (quanti numeri vuole ciascuno) e il controllo di una riga prima
 * di mandarla, cosi' una riga sbagliata non disturba il server. La tabella delle righe
 * che tornano sta nella finestra.
 *
 *   ADD 2 3  ->  5            SUB, MUL, DIV allo stesso modo
 *   DIV 1 0  ->  ERR divisione per zero
 *   FINE     ->  CIAO, e il server chiude
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** I comandi, con quanti numeri vogliono: lo stesso elenco del server. */
    public static final Map<String, Integer> COMANDI = new LinkedHashMap<>();

    static {
        COMANDI.put("ADD", 2);
        COMANDI.put("SUB", 2);
        COMANDI.put("MUL", 2);
        COMANDI.put("DIV", 2);
        COMANDI.put("FINE", 0);
    }

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta. Se gli argomenti sono meno di n, o uno e' vuoto, o ce ne
     * sono quando n e' zero, restituisce null.
     */
    public static String[] argomenti(String resto, int n) {
        resto = resto.strip();
        if (n == 0) return resto.isEmpty() ? new String[0] : null;
        String[] a = resto.split(" ", n);
        if (a.length < n) return null;
        for (int i = 0; i < n; i++) {
            a[i] = a[i].strip();
            if (a[i].isEmpty()) return null;
        }
        return a;
    }

    /**
     * Controlla una riga scritta dall'utente con la tabella: null se va bene, altrimenti
     * il motivo, lo stesso che darebbe il server. Non lancia mai.
     */
    public static String controlla(String riga) {
        String[] campi = riga.strip().split(" ", 2);
        if (campi[0].isEmpty()) return "riga vuota";
        Integer n = COMANDI.get(campi[0].toUpperCase());
        if (n == null) return "operazione sconosciuta: " + campi[0];
        String[] a = argomenti(campi.length > 1 ? campi[1] : "", n);
        if (a == null) return "uso: OP a b, es. ADD 2 3";
        for (String x : a) {
            try {
                Long.parseLong(x);
            } catch (NumberFormatException e) {
                return "numero non valido: " + x;
            }
        }
        return null;
    }

    private Protocollo() {}
}
