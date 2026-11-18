/*
 * Esercizio 8.6 - Quanto e' profondo lo stack
 *
 * Misura la profondita' massima di una ricorsione catturando
 * StackOverflowError, per una funzione senza parametri e per una con quattro
 * parametri long; poi ripete la misura in un secondo thread, creato con una
 * dimensione di stack scelta dal programma (il quarto argomento del
 * costruttore di Thread).
 *
 *     java -Xss256k e06.java      java e06.java      java -Xss4m e06.java
 *
 * Perche' la funzione con piu' parametri scende meno: ogni chiamata occupa un
 * frame sullo stack del thread, e nel frame stanno i parametri e le variabili
 * locali. Quattro long sono 32 byte in piu' per chiamata: a parita' di stack,
 * i frame piu' grandi sono di meno. E' la stessa aritmetica che in C si fa
 * misurando la distanza fra gli indirizzi di due frame consecutivi.
 *
 * Scelte: la classe non e' public, quindi il file puo' chiamarsi e06.java e
 * si lancia senza compilare. Il contatore e' un campo statico, non una
 * variabile locale: dopo l'errore lo stack e' stato smontato e le locali non
 * esistono piu'. Le quattro somme nel corpo di conParametri usano i
 * parametri, cosi' la macchina virtuale non puo' eliminarli.
 *
 * Errore tipico evitato: prendere i numeri per costanti. Dipendono dalla
 * versione di Java, dal sistema e da quanto il compilatore JIT ha gia'
 * ottimizzato il metodo (un metodo compilato usa frame piu' piccoli di uno
 * interpretato): due esecuzioni possono dare valori diversi. Contano l'ordine
 * di grandezza e il rapporto fra le misure. -Xss vale per i thread creati dal
 * programma e, su quasi tutti i sistemi, anche per il thread principale.
 */
class Profondita {
    static int livello;

    static void senzaParametri() {
        livello++;
        senzaParametri();
    }

    static long conParametri(long a, long b, long c, long d) {
        livello++;
        return conParametri(a + 1, b + a, c + b, d + c) + 1;
    }

    static int misura(Runnable ricorsione) {
        livello = 0;
        try {
            ricorsione.run();
        } catch (StackOverflowError e) {
            // lo stack e' gia' stato smontato: qui si puo' di nuovo lavorare
        }
        return livello;
    }

    static void stampa(String dove) {
        int senza = misura(Profondita::senzaParametri);
        int con = misura(() -> conParametri(1, 2, 3, 4));
        System.out.printf("%-28s senza parametri %7d   con 4 long %7d%n", dove, senza, con);
    }

    public static void main(String[] args) throws InterruptedException {
        stampa("thread principale:");

        for (long kb : new long[] { 256, 1024, 8192 }) {
            Thread t = new Thread(null, () -> stampa("thread con " + kb + " KB di stack:"),
                                  "misura", kb * 1024);
            t.start();
            t.join();
        }
    }
}
