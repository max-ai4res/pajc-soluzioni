package it.unibs.pajc.redolfi.e04;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Il protocollo del quiz a piu' giocatori scritto come tabella: la chiave e' il comando,
 * il valore un'Azione che dice quanti argomenti vuole, come si spiega e che cosa fa. La
 * Sessione cerca qui ogni riga del client; /aiuto e' la tabella stessa.
 *
 *   client: /nome N | /r testo | /classifica | /esci | /aiuto
 *   server: INFO testo | BENVENUTO N | DOMANDA n testo | PUNTO nome risposta | TEMPO risposta
 *           | NO | CLASSIFICA a:3,b:1 | FINE a:3,b:1 | ERR motivo
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<Sessione, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",       1, "/nome N      il nome, per primo",       (s, a) -> s.nome(a[0]));
        azione("/r",          1, "/r testo     la risposta alla domanda", (s, a) -> s.risposta(a[0]));
        azione("/classifica", 0, "/classifica  i punti di tutti",         (s, a) -> s.classifica());
        azione("/esci",       0, "/esci        lascia il quiz",           (s, a) -> s.esci());
        azione("/aiuto",      0, "/aiuto       questo elenco",            (s, a) -> s.aiuto());
    }

    private static void azione(String comando, int argomenti, String aiuto,
                               BiConsumer<Sessione, String[]> esegui) {
        COMANDI.put(comando, new Azione(argomenti, aiuto, esegui));
    }

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta, spazi compresi. Se gli argomenti sono meno di n, o uno e'
     * vuoto, o ce ne sono quando n e' zero, restituisce null: la riga non rispetta l'uso
     * del comando e la Sessione risponde ERR con l'aiuto.
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

    private static final Pattern NOME = Pattern.compile("[A-Za-z0-9_]{2,16}");

    /** Lettere, cifre e _, da 2 a 16: il nome finisce nelle righe PUNTO e nella classifica. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /** La classifica come viaggia in CLASSIFICA e FINE: "a:3,b:1", in ordine di nome. */
    public static String classifica(Map<String, Integer> punti) {
        return punti.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    /** Il ritorno: da "a:3,b:1" alla mappa; una voce malformata viene saltata. */
    public static Map<String, Integer> classifica(String s) {
        Map<String, Integer> m = new TreeMap<>();
        if (s.isBlank()) return m;
        for (String voce : s.strip().split(",")) {
            String[] kv = voce.split(":");
            try {
                if (kv.length == 2) m.put(kv[0], Integer.parseInt(kv[1]));
            } catch (NumberFormatException e) {
                // non un numero: la voce non c'e'
            }
        }
        return m;
    }

    private Protocollo() {}
}
