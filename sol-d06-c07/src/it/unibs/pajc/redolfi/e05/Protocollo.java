package it.unibs.pajc.redolfi.e05;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Il protocollo della chat con i file scritto come tabella: la chiave e' il comando, il
 * valore un'Azione che dice quanti argomenti vuole, come si spiega e che cosa fa. La
 * Sessione cerca qui ogni riga che inizia con la barra; /aiuto e' la tabella stessa. Il
 * client usa argomenti e numero per leggere le righe del server, che ha nella sua
 * tabella speculare (ClientFileFinestra).
 *
 * Dal server: INFO, BENVENUTO, UTENTI, MSG, PRIV, ENTRA, ESCE, ERR, CIAO e, per i file,
 * CARICA id (al mittente: metti i byte nel deposito), FILE mittente nome dimensione id
 * (al destinatario) e PRENDI id nome (al destinatario che ha accettato). I byte passano
 * dal Deposito, su un secondo socket a cornici.
 */
public final class Protocollo {

    public static final int PORTA = 1234;
    public static final int PORTA_DEPOSITO = 1235;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<Sessione, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",    1, "/nome N       il nome, per primo",                       (s, a) -> s.nome(a[0]));
        azione("/chi",     0, "/chi          chi e' collegato",                         (s, a) -> s.chi());
        azione("/a",       2, "/a N testo    privato a N",                              (s, a) -> s.privato(a[0], a[1]));
        azione("/invia",   3, "/invia N f d  offri a N il file f, di d byte",           (s, a) -> s.inviaFile(a[0], a[1], a[2]));
        azione("/accetta", 0, "/accetta      ricevi il file che ti e' stato offerto",   (s, a) -> s.accetta());
        azione("/esci",    0, "/esci         saluta",                                   (s, a) -> s.esci());
        azione("/aiuto",   0, "/aiuto        questo elenco",                            (s, a) -> s.aiuto());
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

    /** Lettere, cifre e _, da 2 a 16: il nome finisce nelle righe MSG come secondo campo. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /** Un intero non negativo (dimensione, id) senza lanciare mai: -1 se non lo e'. */
    public static long numero(String s) {
        try {
            long n = Long.parseLong(s.strip());
            return n < 0 ? -1 : n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Protocollo() {}
}
