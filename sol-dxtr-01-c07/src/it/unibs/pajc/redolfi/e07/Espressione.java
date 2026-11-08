package it.unibs.pajc.redolfi.e07;

import java.util.Locale;

import javax.tools.Diagnostic;

/** Da un'espressione scritta dall'utente alla classe che la calcola. */
public class Espressione {

    private static final String MODELLO = """
            package utente;
            import it.unibs.pajc.redolfi.e07.Funzione;
            import it.unibs.pajc.redolfi.e07.Plot;
            import static java.lang.Math.*;
            @Plot(nome = "%s", xMin = %s, xMax = %s)
            public class %s implements Funzione {
                public double f(double x) {
                    return
            %s
                    ;
                }
            }
            """;
    private static final int RIGA_ESPRESSIONE = 9;       // la riga del modello in cui finisce l'espressione
    private static final String RIENTRO = "            "; // i 12 spazi davanti all'espressione
    private static int contatore;

    private final String nomeClasse, sorgente;

    /** Un sorgente per l'espressione, con un nome di classe sempre nuovo. */
    public Espressione(String espressione, String nome, double xMin, double xMax) {
        nomeClasse = "F" + (++contatore);
        sorgente = MODELLO.formatted(nome.replace("\"", "'"), xMin, xMax, nomeClasse, RIENTRO + espressione.strip());
    }

    public String nomeBinario() {
        return "utente." + nomeClasse;
    }

    public String sorgente() {
        return sorgente;
    }

    /** Compila e istanzia; un errore torna gia' riferito alla riga dell'utente. */
    public Funzione compila(Compilatore compilatore) throws ErroreDiCompilazione {
        return compilatore.istanzia(nomeBinario(), sorgente, Funzione.class);
    }

    /** Il messaggio di una diagnostica tradotto sulle coordinate dell'espressione. */
    public static String traduci(Diagnostic<?> d) {
        long riga = d.getLineNumber(), colonna = d.getColumnNumber();
        String messaggio = d.getMessage(Locale.ITALIAN);
        if (riga == RIGA_ESPRESSIONE)
            return "colonna " + Math.max(1, colonna - RIENTRO.length()) + ": " + messaggio;
        if (riga == RIGA_ESPRESSIONE + 1)
            return "alla fine dell'espressione: " + messaggio;
        return "nel modello (riga " + riga + "): " + messaggio;   // un errore nostro, non dell'utente
    }
}
