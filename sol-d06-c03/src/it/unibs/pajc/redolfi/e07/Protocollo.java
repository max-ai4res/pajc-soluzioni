package it.unibs.pajc.redolfi.e07;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Il protocollo del quiz scritto come tabella: la chiave e' il comando, il valore
 * un'Azione che dice quanti argomenti vuole, come si spiega e che cosa fa sul ServerQuiz
 * del client che l'ha scritto.
 *
 *   client: /nome N | /r testo | /classifica | /esci | /aiuto
 *   server: INFO testo | BENVENUTO N | DOMANDA n testo | OK punti | NO risposta
 *           | PUNTI punti | FINE punti | ERR motivo
 *
 * A passo guidato dal server: dopo una DOMANDA il server aspetta una /r; /classifica e'
 * lecito in qualunque momento e non e' una risposta.
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<ServerQuiz, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",       1, "/nome N      il nome, per primo",         (q, a) -> q.nome(a[0]));
        azione("/r",          1, "/r testo     la risposta alla domanda",   (q, a) -> q.risposta(a[0]));
        azione("/classifica", 0, "/classifica  i punti finora",             (q, a) -> q.classifica());
        azione("/esci",       0, "/esci        chiude con il totale",       (q, a) -> q.esci());
        azione("/aiuto",      0, "/aiuto       questo elenco",              (q, a) -> q.aiuto());
    }

    private static void azione(String comando, int argomenti, String aiuto,
                               BiConsumer<ServerQuiz, String[]> esegui) {
        COMANDI.put(comando, new Azione(argomenti, aiuto, esegui));
    }

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta, spazi compresi (la risposta "la capitale e' Roma" e' un solo
     * argomento). Se gli argomenti sono meno di n, o uno e' vuoto, o ce ne sono quando n
     * e' zero, restituisce null: il server risponde ERR con l'aiuto.
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

    private static final Pattern NOME = Pattern.compile("[\\p{L}\\p{N}_]{2,16}");

    /** Lettere, cifre e _, da 2 a 16: e' un controllo di valore, e sta nell'azione nome. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    private Protocollo() {}
}
