/* Modulo leggi: implementazione (esercizio 2.8) */
#include <stdio.h>
#include "e08_leggi.h"

size_t leggi_interi(int *v, size_t max) {
    size_t n = 0;
    /* scanf restituisce 1 finche' legge un intero: a EOF o a un token che
     * non e' un numero restituisce EOF o 0 e il ciclo si ferma. */
    while (n < max && scanf("%d", &v[n]) == 1)
        n++;
    return n;
}
