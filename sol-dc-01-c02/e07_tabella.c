/* Modulo tabella: implementazione (esercizio 2.7) */
#include <stdio.h>
#include "e07_tabella.h"

void tabella_stampa(int n, int fino_a) {
    for (int i = 1; i <= fino_a; i++)
        printf("%3d x %2d = %4d\n", n, i, n * i);
}
