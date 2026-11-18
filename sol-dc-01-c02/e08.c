/*
 * Esercizio 2.8 - Due programmi, una cartella (programma "somma")
 *
 * Due eseguibili con due main (nella dispensa somma.c e prodotto.c, qui e08.c
 * ed e08_prodotto.c) condividono il modulo leggi.h/leggi.c (e08_leggi.*),
 * che legge tutti gli interi da stdin in un array di dimensione fissa e
 * restituisce quanti ne ha letti. Questo programma li somma.
 *
 * Il Makefile della cartella ha una regola esplicita per bin/e08 e una per
 * bin/e08_prodotto, entrambe con e08_leggi.c fra le dipendenze, e un bersaglio
 * `all` in testa che dipende da tutti gli eseguibili: `make` da solo costruisce
 * il PRIMO bersaglio del file, e se quello fosse bin/e08, bin/e08_prodotto non
 * verrebbe mai fatto. `all` non ha ricetta e sta in .PHONY. Il Makefile della
 * dispensa, con gli oggetti condivisi, sarebbe:
 *
 *   CC     = gcc
 *   CFLAGS = -std=c17 -Wall -Wextra -Werror -g
 *
 *   all: bin/somma bin/prodotto
 *
 *   bin/somma: somma.o leggi.o
 *   	$(CC) $(CFLAGS) -o $@ $^
 *   bin/prodotto: prodotto.o leggi.o
 *   	$(CC) $(CFLAGS) -o $@ $^
 *
 *   %.o: %.c leggi.h
 *   	$(CC) $(CFLAGS) -c $<
 *
 *   clean:
 *   	rm -f bin/somma bin/prodotto *.o
 *
 *   .PHONY: all clean
 *
 * leggi.o viene compilato una volta sola e linkato in entrambi.
 *
 * Prova: echo 1 2 3 | ./bin/e08   stampa 6.
 */
#include <stdio.h>
#include "e08_leggi.h"

#define MAX 100

int main(void) {
    int v[MAX];
    size_t n = leggi_interi(v, MAX);

    long somma = 0;
    for (size_t i = 0; i < n; i++)
        somma += v[i];

    printf("%ld\n", somma);
    return 0;
}
