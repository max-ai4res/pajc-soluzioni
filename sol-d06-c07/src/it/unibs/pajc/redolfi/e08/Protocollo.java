package it.unibs.pajc.redolfi.e08;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Il protocollo di WhatsPAJC completa scritto come tabella: la chiave e' il comando, il
 * valore un'Azione che dice quanti argomenti vuole, come si spiega e che cosa fa. La
 * Sessione cerca qui ogni riga che inizia con la barra; /aiuto e' la tabella stessa.
 * Aggiungere un comando e' una riga qui e un metodo in Sessione. Il client usa
 * argomenti, nomeValido e stanze, e per le righe del server ha la sua tabella
 * speculare nel ChatController.
 *
 * Dal server arrivano righe con una parola maiuscola in testa: INFO, BENVENUTO, UTENTI,
 * MSG, PRIV, ENTRA, ESCE, ERR, CIAO, STANZA (sei entrato nella stanza), STANZE
 * (generale:3,pajc:1), PONG, FOTO mittente nome; il resto della riga e' l'argomento.
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<Sessione, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",   1, "/nome N     il nome, per primo",                        (s, a) -> s.nome(a[0]));
        azione("/chi",    0, "/chi        chi e' nella stanza",                       (s, a) -> s.chi());
        azione("/a",      2, "/a N testo  privato a N, anche in un'altra stanza",     (s, a) -> s.privato(a[0], a[1]));
        azione("/entra",  1, "/entra S    passa alla stanza S (la crea se non c'e')", (s, a) -> s.entra(a[0]));
        azione("/stanze", 0, "/stanze     le stanze e quanti ci sono",                (s, a) -> s.stanze());
        azione("/ping",   0, "/ping       segnala che ci sei (PONG)",                 (s, a) -> s.ping());
        azione("/esci",   0, "/esci       saluta",                                    (s, a) -> s.esci());
        azione("/aiuto",  0, "/aiuto      questo elenco",                             (s, a) -> s.aiuto());
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
    private static final Pattern STANZA = Pattern.compile("[a-z0-9-]{2,16}");

    /** Lettere, cifre e _, da 2 a 16: il nome finisce nelle righe MSG come secondo campo. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /** Minuscole, cifre e -, da 2 a 16: la stanza finisce in STANZE con i due punti dietro. */
    public static boolean stanzaValida(String stanza) {
        return STANZA.matcher(stanza).matches();
    }

    /** Le stanze come viaggiano in STANZE: "generale:3,pajc:1", in ordine di nome. */
    public static String stanze(Map<String, Integer> utentiPerStanza) {
        return utentiPerStanza.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    /** Il ritorno: da "generale:3,pajc:1" alla mappa; una voce malformata viene saltata. */
    public static Map<String, Integer> stanze(String s) {
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
