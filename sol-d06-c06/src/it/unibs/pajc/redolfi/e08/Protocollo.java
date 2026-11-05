package it.unibs.pajc.redolfi.e08;

import java.util.regex.Pattern;

/**
 * La parte del protocollo di WhatsPAJC che serve al client. La tabella dei comandi sta
 * nel server: il client manda le righe dell'utente come sono (/nome N, /chi, /a N testo,
 * /esci, /aiuto, o un messaggio per tutti) e interpreta le righe del server con la
 * tabella speculare del Controller. Qui restano le due cose comuni ai due lati: come si
 * spezza il resto di una riga in argomenti e che cosa e' un nome valido.
 *
 * Dal server arrivano righe con una parola maiuscola in testa: INFO, BENVENUTO, UTENTI,
 * MSG, PRIV, ENTRA, ESCE, ERR, CIAO; il resto della riga e' l'argomento.
 */
public final class Protocollo {

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta, spazi compresi. Se gli argomenti sono meno di n, o uno e'
     * vuoto, o ce ne sono quando n e' zero, restituisce null: la riga non rispetta l'uso.
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

    /** Lettere, cifre e _, da 2 a 16: un nome finisce nelle righe MSG come secondo campo. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    private Protocollo() {}
}
