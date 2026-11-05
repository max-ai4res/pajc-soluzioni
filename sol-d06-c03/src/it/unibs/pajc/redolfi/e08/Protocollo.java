package it.unibs.pajc.redolfi.e08;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Il protocollo di WhatsPAJC v1 esteso con /storia n (Esercizio 3.8): la tabella dei
 * comandi con una riga in piu'. Il valore di n (un intero positivo) si controlla
 * nell'azione con interoPositivo, che non lancia mai; la forma della riga la controlla
 * argomenti come per tutti gli altri comandi.
 *
 * Dal server arrivano righe con una parola maiuscola in testa: INFO, BENVENUTO, UTENTI,
 * MSG, PRIV, ERR, CIAO; il resto della riga e' l'argomento. Le righe della storia sono
 * INFO, una per riga del registro.
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare. */
    public record Azione(int argomenti, String aiuto, BiConsumer<ServerChatRegistro, String[]> esegui) {}

    /** I comandi, nell'ordine in cui /aiuto li elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("/nome",   1, "/nome N     il nome, per primo",             (s, a) -> s.nome(a[0]));
        azione("/chi",    0, "/chi        chi e' collegato",               (s, a) -> s.chi());
        azione("/a",      2, "/a N testo  privato a N",                    (s, a) -> s.privato(a[0], a[1]));
        azione("/storia", 1, "/storia n   le ultime n righe del registro", (s, a) -> s.storia(a[0]));
        azione("/esci",   0, "/esci       saluta",                         (s, a) -> s.esci());
        azione("/aiuto",  0, "/aiuto      questo elenco",                  (s, a) -> s.aiuto());
    }

    private static void azione(String comando, int argomenti, String aiuto,
                               BiConsumer<ServerChatRegistro, String[]> esegui) {
        COMANDI.put(comando, new Azione(argomenti, aiuto, esegui));
    }

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta, spazi compresi. Se gli argomenti sono meno di n, o uno e'
     * vuoto, o ce ne sono quando n e' zero, restituisce null: la riga non rispetta l'uso
     * del comando e il server risponde ERR con l'aiuto.
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

    /** Lettere, cifre e _, da 2 a 16: il nome finisce nelle righe MSG come secondo campo. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    /** Il valore di n per /storia: un intero maggiore di zero, altrimenti null. Non lancia mai. */
    public static Integer interoPositivo(String testo) {
        try {
            int n = Integer.parseInt(testo);
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Protocollo() {}
}
