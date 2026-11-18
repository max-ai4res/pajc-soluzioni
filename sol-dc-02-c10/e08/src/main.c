/*
 * Esercizio 10.8 - I codici d'errore
 *
 * Il modulo errore: un solo elenco, ERRORI_X in include/errore.h, da cui
 * escono tre cose che devono restare allineate: l'enum dei codici, la tabella
 * dei nomi (con # che trasforma ERR_FILE in "ERR_FILE") e la tabella dei
 * messaggi. Aggiungere un errore e' aggiungere UNA riga all'elenco: enum e
 * tabelle si aggiornano da soli, e non possono disallinearsi.
 *
 * Questo main stampa la tabella; tests/test_errore.c verifica con assert che
 * ogni codice abbia nome e messaggio non vuoti e che un codice fuori
 * dall'intervallo sia gestito (make test).
 *
 * Scelte: l'elenco sta nell'header perche' l'enum serve a chi usa il modulo;
 * le tabelle stanno nel sorgente, static. X viene cancellata con #undef dopo
 * ogni uso, anche nell'header: lasciata definita arriverebbe in ogni file che
 * lo include. N_ERRORI, ultimo valore dell'enum, e' il numero dei codici, e
 * due _Static_assert controllano a compilazione che le tabelle abbiano quella
 * lunghezza.
 *
 * Errore tipico evitato: usare il codice come indice senza controllarlo. Un
 * enum e' un intero, e da fuori puo' arrivare 99 o -1: NOMI[99] leggerebbe
 * oltre la tabella. errore_nome ed errore_messaggio controllano l'intervallo.
 */
#include <stdio.h>

#include "errore.h"

int main(void) {
    for (int e = 0; e < N_ERRORI; e++)
        printf("%d  %-14s %s\n", e, errore_nome((Errore)e), errore_messaggio((Errore)e));
    printf("%d  %-14s %s\n", 99, errore_nome((Errore)99), errore_messaggio((Errore)99));
    return 0;
}
