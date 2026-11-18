/*
 * Esercizio 4.7 - Due risultati
 *
 * min_max(const int *v, size_t n, int *min, int *max) scrive minimo e massimo
 * dell'array nelle due variabili del chiamante, attraverso i puntatori: e' il
 * modo del C per restituire due valori. L'array e' const perche' la funzione
 * lo legge soltanto.
 *
 * Decisione per n == 0: un array vuoto non ha minimo ne' massimo, quindi la
 * funzione NON scrive nulla in *min e *max e restituisce subito (il chiamante
 * trova i valori che c'erano prima; qui il main li inizializza a 0 e controlla
 * n prima di stamparli). Le alternative erano scrivere INT_MAX/INT_MIN come
 * "minimo e massimo dell'insieme vuoto", o restituire un bool di esito: la
 * seconda e' la forma dell'esercizio 4.8.
 *
 * Il main legge da stdin fino a venti numeri, o fino alla parola `fine`, o a
 * EOF (scanf restituisce 0 su una parola e EOF a fine input: in entrambi i casi
 * il ciclo si ferma). Prova: printf '3 -1 7 fine\n' | ./bin/e07
 */
#include <stdio.h>

#define MAX_NUMERI 20

void min_max(const int *v, size_t n, int *min, int *max) {
    if (n == 0)
        return;                                /* nulla da dire: non scrive */
    *min = v[0];
    *max = v[0];
    for (size_t i = 1; i < n; i++) {
        if (v[i] < *min)
            *min = v[i];
        if (v[i] > *max)
            *max = v[i];
    }
}

int main(void) {
    int v[MAX_NUMERI];
    size_t n = 0;

    printf("numeri interi (max %d), poi `fine`: ", MAX_NUMERI);
    fflush(stdout);
    while (n < MAX_NUMERI && scanf("%d", &v[n]) == 1)
        n++;

    printf("\nletti %zu numeri\n", n);
    if (n == 0) {
        printf("nessun numero: minimo e massimo non definiti\n");
        return 0;
    }

    int min = 0, max = 0;
    min_max(v, n, &min, &max);
    printf("minimo %d, massimo %d\n", min, max);
    return 0;
}
