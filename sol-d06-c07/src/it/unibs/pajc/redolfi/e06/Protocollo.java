package it.unibs.pajc.redolfi.e06;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Il protocollo del tris in rete scritto come tabella: la chiave e' il comando, il
 * valore un'Azione che dice quanti argomenti vuole (per /guarda l'argomento e'
 * facoltativo: senza, l'elenco delle partite), come si spiega e che cosa fa. La Sessione
 * cerca qui ogni riga; /aiuto e' la tabella stessa. I client usano argomenti e
 * coordinata per leggere le righe del server, che hanno nella loro tabella speculare.
 *
 *   client: /nome N | /m riga colonna (0..2) | /guarda [a-b] | /esci | /aiuto
 *   server: INFO testo | BENVENUTO N | PARTITA X Giulia | TOCCA | MOSSA X 1 2
 *           | VINCE X | PATTA | ABBANDONO N | ERR motivo | CIAO
 *           | PARTITE a-b,c-d | STATO X.O...... O   (la griglia riga per riga, poi il turno)
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi (facoltativi: anche nessuno), aiuto, che cosa fare. */
    public record Azione(int argomenti, boolean facoltativi, String aiuto,
                         BiConsumer<Sessione, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",   1, false, "/nome N      il nome, per primo: poi si aspetta un avversario", (s, a) -> s.nome(a[0]));
        azione("/m",      2, false, "/m riga col  la mossa, da 0 a 2",                              (s, a) -> s.mossa(a[0], a[1]));
        azione("/guarda", 1, true,  "/guarda [P]  le partite in corso, o segui la partita P",       (s, a) -> s.guarda(a.length == 0 ? null : a[0]));
        azione("/esci",   0, false, "/esci        saluta",                                          (s, a) -> s.esci());
        azione("/aiuto",  0, false, "/aiuto       questo elenco",                                   (s, a) -> s.aiuto());
    }

    private static void azione(String comando, int argomenti, boolean facoltativi, String aiuto,
                               BiConsumer<Sessione, String[]> esegui) {
        COMANDI.put(comando, new Azione(argomenti, facoltativi, aiuto, esegui));
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

    /** Lettere, cifre e _, da 2 a 16: due nomi uniti da - sono il nome di una partita. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /** Una riga o colonna della griglia, da 0 a 2, senza lanciare mai: -1 se non lo e'. */
    public static int coordinata(String s) {
        return switch (s.strip()) {
            case "0" -> 0;
            case "1" -> 1;
            case "2" -> 2;
            default -> -1;
        };
    }

    private Protocollo() {}
}
