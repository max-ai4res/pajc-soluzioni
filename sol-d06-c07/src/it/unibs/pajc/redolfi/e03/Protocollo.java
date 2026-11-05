package it.unibs.pajc.redolfi.e03;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Il protocollo della calcolatrice remota scritto come tabella: la chiave e' l'operazione
 * cosi' come arriva sul filo, il valore un'Azione che dice quanti operandi vuole, come si
 * spiega e che cosa fa. L'azione riceve gli operandi gia' convertiti in numeri: la
 * conversione sta in un posto solo (numeri) e non lancia mai. Aggiungere un'operazione e'
 * una riga qui e un metodo in Sessione; AIUTO la elenca da solo. Il client in finestra
 * usa argomenti per leggere STAT e ha la sua tabella speculare delle risposte.
 *
 *   ADD 2 3  ->  5            SUB, MUL, DIV allo stesso modo
 *   DIV 1 0  ->  ERR divisione per zero
 *   STAT     ->  STAT operazioni client   (eseguite in tutto, collegati adesso)
 *   AIUTO    ->  un INFO per ogni riga della tabella
 *   FINE     ->  CIAO, e il server chiude
 */
public final class Protocollo {

    public static final int PORTA = 1234;

    /** Una riga della tabella: operandi attesi, testo per AIUTO, che cosa fare. */
    public record Azione(int operandi, String aiuto, BiConsumer<Sessione, long[]> esegui) {}

    /** Le operazioni, nell'ordine in cui AIUTO le elenca. */
    public static final Map<String, Azione> COMANDI = new LinkedHashMap<>();

    static {
        azione("ADD",   2, "ADD a b   la somma",                         (s, o) -> s.risultato(o[0] + o[1]));
        azione("SUB",   2, "SUB a b   la differenza",                    (s, o) -> s.risultato(o[0] - o[1]));
        azione("MUL",   2, "MUL a b   il prodotto",                      (s, o) -> s.risultato(o[0] * o[1]));
        azione("DIV",   2, "DIV a b   il quoziente intero",              (s, o) -> s.dividi(o[0], o[1]));
        azione("STAT",  0, "STAT      operazioni eseguite e client collegati", (s, o) -> s.stat());
        azione("FINE",  0, "FINE      saluta e chiude",                  (s, o) -> s.fine());
        azione("AIUTO", 0, "AIUTO     questo elenco",                    (s, o) -> s.aiuto());
    }

    private static void azione(String comando, int operandi, String aiuto,
                               BiConsumer<Sessione, long[]> esegui) {
        COMANDI.put(comando, new Azione(operandi, aiuto, esegui));
    }

    /**
     * Spezza il resto della riga in n argomenti: split con il limite, cosi' l'ultimo e'
     * tutto cio' che resta. Se gli argomenti sono meno di n, o uno e' vuoto, o ce ne sono
     * quando n e' zero, restituisce null: la riga non rispetta l'uso dell'operazione e la
     * Sessione risponde ERR con l'aiuto.
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

    /**
     * Converte gli argomenti in numeri interi, senza lanciare mai: null se anche uno solo
     * non e' un numero. E' il controllo di valore, separato da quello di forma di argomenti,
     * cosi' "ADD 2" e "ADD due 3" ricevono due ERR diversi.
     */
    public static long[] numeri(String[] argomenti) {
        long[] n = new long[argomenti.length];
        for (int i = 0; i < argomenti.length; i++) {
            try {
                n[i] = Long.parseLong(argomenti[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return n;
    }

    private Protocollo() {}
}
