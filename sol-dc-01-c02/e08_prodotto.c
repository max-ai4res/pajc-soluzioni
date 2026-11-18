/*
 * Esercizio 2.8 - Due programmi, una cartella (programma "prodotto")
 *
 * Secondo main della coppia: legge gli interi con lo stesso modulo e08_leggi
 * e ne stampa il prodotto. Con nessun numero il prodotto vuoto vale 1.
 * long long per non traboccare subito; con molti numeri grandi trabocca
 * comunque: e' un esercizio sul Makefile, non sull'aritmetica.
 *
 * Prova: echo 2 3 4 | ./bin/e08_prodotto   stampa 24.
 */
#include <stdio.h>
#include "e08_leggi.h"

#define MAX 100

int main(void) {
    int v[MAX];
    size_t n = leggi_interi(v, MAX);

    long long prodotto = 1;
    for (size_t i = 0; i < n; i++)
        prodotto *= v[i];

    printf("%lld\n", prodotto);
    return 0;
}
