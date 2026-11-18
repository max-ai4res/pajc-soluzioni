/* e03_matrice.h: una matrice r x c di double come blocco unico sullo heap.
 * Due rappresentazioni dello stesso blocco: il puntatore piatto double *, indicizzato
 * m[i * c + j], e il puntatore a righe double (*m)[c], che si ottiene dal primo con il
 * cast (double (*)[c])m e si usa come m[i][j]. Le funzioni che hanno le dimensioni nei
 * parametri (riempi, traccia) usano la seconda; le altre la prima.
 * Contratto: ogni funzione che restituisce un double * restituisce un blocco NUOVO che
 * il chiamante libera con matrice_free; il risultato non e' mai uno degli operandi. */
#ifndef E03_MATRICE_H
#define E03_MATRICE_H

#include <stddef.h>

/* r * c double azzerati; NULL se la memoria non basta. */
double *matrice_new(size_t r, size_t c);
void matrice_free(double *m);

/* m[i][j] = i + j, attraverso la vista a righe (VLA nel parametro: non alloca). */
void riempi(size_t r, size_t c, double m[r][c]);

/* La trasposta (c x r) in un blocco nuovo. */
double *trasposta(size_t r, size_t c, const double *m);

/* a (ra x ca) per b (rb x cb) in un blocco nuovo (ra x cb); NULL se ca != rb o se la
 * memoria non basta. a e b non vengono toccate: il risultato e' sempre un blocco a parte. */
double *prodotto(size_t ra, size_t ca, const double *a,
                 size_t rb, size_t cb, const double *b);

void stampa(size_t r, size_t c, const double *m);

/* Somma della diagonale di una matrice quadrata, vista a righe. */
double traccia(size_t n, double m[n][n]);

#endif
