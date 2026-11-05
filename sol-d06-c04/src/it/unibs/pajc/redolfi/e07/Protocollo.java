package it.unibs.pajc.redolfi.e07;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Il protocollo di WhatsPAJC con le stanze, scritto come tabella: la chiave e' il
 * comando, il valore un'Azione che dice quanti argomenti vuole, come si spiega e che
 * cosa fa. Rispetto alla tabella della dispensa ci sono due righe in piu', /entra e
 * /stanze, e un controllo di valore in piu', il nome di stanza. /aiuto e' la tabella
 * stessa.
 *
 * Dal server arrivano righe con una parola maiuscola in testa: INFO, BENVENUTO, UTENTI,
 * MSG, PRIV, ENTRA, ESCE, ERR, CIAO come nella dispensa, e in piu' STANZA s (sei
 * entrato in s) e STANZE generale:3,giochi:1 (le stanze con quanti ci sono).
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<Sessione, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",   1, "/nome N     il nome, per primo",                     (s, a) -> s.nome(a[0]));
        azione("/chi",    0, "/chi        chi e' nella tua stanza",                (s, a) -> s.chi());
        azione("/a",      2, "/a N testo  privato a N, anche in un'altra stanza",  (s, a) -> s.privato(a[0], a[1]));
        azione("/entra",  1, "/entra S    passa alla stanza S (la crea se non c'e')", (s, a) -> s.entra(a[0]));
        azione("/stanze", 0, "/stanze     le stanze e quanti ci sono",             (s, a) -> s.stanze());
        azione("/esci",   0, "/esci       saluta",                                 (s, a) -> s.esci());
        azione("/aiuto",  0, "/aiuto      questo elenco",                          (s, a) -> s.aiuto());
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

    private static final Pattern NOME = Pattern.compile("[\\p{L}\\p{N}_]{2,16}");
    private static final Pattern STANZA = Pattern.compile("[a-z0-9-]{2,16}");

    /** Lettere, cifre e _, da 2 a 16: il nome finisce nelle righe MSG come secondo campo. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /**
     * Minuscole, cifre e trattino, da 2 a 16: il nome di stanza finisce nella riga
     * STANZE separato da : e da , e quei due caratteri non puo' contenerli.
     */
    public static boolean stanzaValida(String stanza) {
        return STANZA.matcher(stanza).matches();
    }

    private Protocollo() {}
}
