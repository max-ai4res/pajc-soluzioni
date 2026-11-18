/*
 * Esercizio 5.7 - La matrice come parametro
 *
 * r e c arrivano dalla riga di comando (senza argomenti: 3 4). La matrice
 * e' UN solo blocco di r righe da c interi: int (*m)[c] = malloc(r * sizeof *m),
 * dove sizeof *m e' la misura di una riga (c * sizeof(int)). Le funzioni
 * ricevono r e c e poi il parametro VLA m[r][c], cosi' dentro m[i][j] e'
 * l'indicizzazione normale e il compilatore fa lui il conto i * c + j.
 *
 * Scelte: stampa_matrice si usa solo fino a 100 celle, altrimenti l'output
 * sarebbe illeggibile; somma_matrice restituisce long long perche' con r e c
 * di qualche migliaio la somma 0 + 1 + ... + (rc - 1) supera un int (con
 * 3000 x 3000 vale circa 4e13) e l'overflow con segno e' comportamento
 * indefinito. Il valore atteso e' N (N - 1) / 2 con N = r * c.
 *
 * Con ./bin/e07 3000 3000 il programma vive (36 MB sullo heap); lo stesso
 * come VLA locale, int m[r][c], sarebbe 36 MB sullo stack da 8 MB: segfault.
 */
#include <stdio.h>
#include <stdlib.h>

void stampa_matrice(size_t r, size_t c, const int m[r][c]) {
    for (size_t i = 0; i < r; i++) {
        for (size_t j = 0; j < c; j++) printf("%4d", m[i][j]);
        printf("\n");
    }
}

long long somma_matrice(size_t r, size_t c, const int m[r][c]) {
    long long s = 0;
    for (size_t i = 0; i < r; i++)
        for (size_t j = 0; j < c; j++) s += m[i][j];
    return s;
}

static size_t leggi_dimensione(const char *testo, size_t predefinita) {
    if (testo == NULL) return predefinita;
    char *fine;
    unsigned long v = strtoul(testo, &fine, 10);
    if (*fine != '\0' || v == 0) {
        fprintf(stderr, "dimensione non valida: %s\n", testo);
        exit(EXIT_FAILURE);
    }
    return (size_t)v;
}

int main(int argc, char *argv[]) {
    size_t r = leggi_dimensione(argc > 1 ? argv[1] : NULL, 3);
    size_t c = leggi_dimensione(argc > 2 ? argv[2] : NULL, 4);

    int (*m)[c] = malloc(r * sizeof *m);      /* r righe da c int, un blocco */
    if (m == NULL) {
        fprintf(stderr, "memoria insufficiente per %zu x %zu\n", r, c);
        return EXIT_FAILURE;
    }
    for (size_t i = 0; i < r; i++)
        for (size_t j = 0; j < c; j++) m[i][j] = (int)(i * c + j);

    printf("matrice %zu x %zu (%zu byte)\n", r, c, r * sizeof *m);
    if (r * c <= 100) stampa_matrice(r, c, m);
    else printf("(troppo grande da stampare: m[0][0] = %d, m[%zu][%zu] = %d)\n",
                m[0][0], r - 1, c - 1, m[r - 1][c - 1]);

    long long n = (long long)(r * c);
    printf("somma ottenuta: %lld\n", somma_matrice(r, c, m));
    printf("somma attesa:   %lld\n", n * (n - 1) / 2);

    free(m);
    m = NULL;
    return 0;
}
