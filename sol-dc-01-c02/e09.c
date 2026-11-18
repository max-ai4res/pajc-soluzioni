/*
 * Esercizio 2.9 - debug e asan
 *
 * Il programma stampa "debug acceso" solo se e' stato compilato con -DDEBUG:
 * la printf sta dentro #ifdef DEBUG ... #endif, e senza la macro il
 * preprocessore la toglie prima ancora che il compilatore la veda. Il resto
 * del programma (qualche somma) serve solo a dare qualcosa da fare.
 *
 * Nel Makefile della cartella i bersagli chiesti dall'esercizio sono:
 *   bin/e09   (normale, costruito anche da `make`),
 *   debug     ricompila bin/e09 con -DDEBUG in piu',
 *   asan      ricompila tutto con -fsanitize=address,
 *   run       lancia bin/e09.
 * Tutti e tre producono lo STESSO eseguibile bin/e09, con flag diversi.
 *
 * Perche' `make` da solo, dopo `make debug`, non ricompila: make decide dalle
 * DATE, non dai flag. Dopo `make debug` il file bin/e09 esiste ed e' piu'
 * recente di e09.c, quindi per make e' aggiornato e non c'e' nulla da fare:
 * make non sa con quali opzioni e' stato prodotto. Per questo `debug`
 * cancella prima bin/e09 e poi lo rifa' (e `asan` passa da `clean`), e per
 * tornare alla versione normale serve `make clean && make`.
 *
 * Verifica: make debug && make run  -> stampa "debug acceso"
 *           make clean && make run  -> non la stampa
 */
#include <stdio.h>

int main(void) {
#ifdef DEBUG
    printf("debug acceso\n");
#endif
    int somma = 0;
    for (int i = 1; i <= 10; i++)
        somma += i;
    printf("somma 1..10 = %d (atteso 55)\n", somma);
    return 0;
}
