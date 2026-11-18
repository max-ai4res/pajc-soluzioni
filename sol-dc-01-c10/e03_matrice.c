/* e03_matrice.c: le definizioni del modulo (vedi e03_matrice.h). */
#include <stdio.h>
#include <stdlib.h>
#include "e03_matrice.h"

double *matrice_new(size_t r, size_t c) {
    return calloc(r * c, sizeof(double));      /* azzerati: il prodotto accumula */
}

void matrice_free(double *m) {
    free(m);
}

void riempi(size_t r, size_t c, double m[r][c]) {
    for (size_t i = 0; i < r; i++)
        for (size_t j = 0; j < c; j++)
            m[i][j] = (double)(i + j);         /* il compilatore calcola i * c + j */
}

double *trasposta(size_t r, size_t c, const double *m) {
    double *t = matrice_new(c, r);
    if (t == NULL) return NULL;
    for (size_t i = 0; i < r; i++)
        for (size_t j = 0; j < c; j++)
            t[j * r + i] = m[i * c + j];
    return t;
}

double *prodotto(size_t ra, size_t ca, const double *a,
                 size_t rb, size_t cb, const double *b) {
    if (ca != rb) return NULL;                 /* le dimensioni non tornano */
    double *p = matrice_new(ra, cb);
    if (p == NULL) return NULL;
    /* ordine i, k, j: la riga di b e quella di p scorrono in avanti, e la cache
     * lavora bene anche con 2000 x 2000 */
    for (size_t i = 0; i < ra; i++) {
        double *riga_p = p + i * cb;
        for (size_t k = 0; k < ca; k++) {
            double aik = a[i * ca + k];
            const double *riga_b = b + k * cb;
            for (size_t j = 0; j < cb; j++)
                riga_p[j] += aik * riga_b[j];
        }
    }
    return p;
}

void stampa(size_t r, size_t c, const double *m) {
    for (size_t i = 0; i < r; i++) {
        for (size_t j = 0; j < c; j++)
            printf("%8g", m[i * c + j]);
        printf("\n");
    }
}

double traccia(size_t n, double m[n][n]) {
    double t = 0;
    for (size_t i = 0; i < n; i++) t += m[i][i];
    return t;
}
