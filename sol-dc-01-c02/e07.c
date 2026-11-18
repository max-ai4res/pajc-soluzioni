/*
 * Esercizio 2.7 - La tabellina in tre file
 *
 * Nella dispensa i tre file si chiamano tabella.h, tabella.c e main.c; qui,
 * per convivere con gli altri esercizi della cartella, sono e07_tabella.h,
 * e07_tabella.c e questo e07.c. Il main legge n da argv[1] con atoi e stampa
 * la tabellina fino a 10 chiamando tabella_stampa del modulo.
 *
 * Il Makefile "a mano" chiesto dall'esercizio, nella forma minima a tre
 * regole esplicite e senza variabili (con i nomi della dispensa):
 *
 *   bin/tabella: main.o tabella.o
 *   	gcc -o bin/tabella main.o tabella.o
 *
 *   main.o: main.c tabella.h
 *   	gcc -std=c17 -Wall -Wextra -Werror -g -c main.c
 *
 *   tabella.o: tabella.c tabella.h
 *   	gcc -std=c17 -Wall -Wextra -Werror -g -c tabella.c
 *
 * Verifica: `touch main.c && make -n` mostra solo la ricompilazione di main.o
 * e il link di bin/tabella; tabella.o non compare perche' nessuna delle sue
 * dipendenze (tabella.c, tabella.h) e' piu' recente di lui. Toccando invece
 * tabella.h, make -n rifa' entrambi gli oggetti: lo hanno tutti e due fra
 * le dipendenze.
 *
 * Errore tipico evitato: dimenticare tabella.h fra le dipendenze dei .o; make
 * non vedrebbe le modifiche all'header e lascerebbe oggetti vecchi.
 */
#include <stdio.h>
#include <stdlib.h>
#include "e07_tabella.h"

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "uso: %s n\n", argv[0]);
        return 1;
    }
    int n = atoi(argv[1]);
    printf("Tabellina del %d:\n", n);
    tabella_stampa(n, 10);
    return 0;
}
