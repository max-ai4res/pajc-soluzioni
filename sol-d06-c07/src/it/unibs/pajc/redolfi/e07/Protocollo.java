package it.unibs.pajc.redolfi.e07;

import java.util.List;

/**
 * Il protocollo del sondaggio, righe di testo:
 *   client: /nome N | /voto k
 *   server: BENVENUTO N | DOMANDA testo|risposta1|risposta2|... | VOTI 3,5,1 | CHIUSA | OK | ERR motivo
 * Scoperta in UDP sulla porta 1234: "SONDAGGIO?" -> "SONDAGGIO porta".
 */
public final class Protocollo {
    public static final int PORTA = 1234;
    private Protocollo() {}

    public static String domanda(String testo, List<String> risposte) { return "DOMANDA " + testo + "|" + String.join("|", risposte); }
    public static String voti(int[] conteggi) {
        StringBuilder b = new StringBuilder("VOTI ");
        for (int i = 0; i < conteggi.length; i++) b.append(i > 0 ? "," : "").append(conteggi[i]);
        return b.toString();
    }
    /** "DOMANDA testo|r1|r2" -> [testo, r1, r2]; null se non e' una DOMANDA. */
    public static List<String> parseDomanda(String riga) {
        if (!riga.startsWith("DOMANDA ")) return null;
        return List.of(riga.substring(8).split("\\|"));
    }
    public static int[] parseVoti(String riga) {
        String[] v = riga.substring(5).split(",");
        int[] c = new int[v.length];
        for (int i = 0; i < v.length; i++) c[i] = Integer.parseInt(v[i].strip());
        return c;
    }
}
