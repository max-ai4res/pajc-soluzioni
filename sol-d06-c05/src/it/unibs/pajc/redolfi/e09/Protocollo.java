package it.unibs.pajc.redolfi.e09;

import java.util.regex.Pattern;

/**
 * Il protocollo del tris in rete, dal lato del client. I comandi partono come righe e la
 * tabella che li interpreta sta nel server; qui c'e' cio' che serve a chi le righe le
 * riceve: argomenti, per spezzare il resto di una riga del server, e nomeValido. La
 * tabella delle righe del server sta nella finestra.
 *
 *   client: /nome N | /m riga colonna (0..2) | /esci
 *   server: INFO testo | BENVENUTO N | PARTITA X Giulia | TOCCA | MOSSA X 1 2
 *           | VINCE X | PATTA | ABBANDONO N | ERR motivo | CIAO
 */
public final class Protocollo {

    public static final int PORTA = 1234;

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

    private static final Pattern NOME = Pattern.compile("[\\p{L}\\p{N}_]{2,16}");

    /** Lettere, cifre e _, da 2 a 16, come vuole il server. */
    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    private Protocollo() {}
}
