/* Esercizio 10.3 - La matrice dinamica
 * Il modulo e03_matrice.h / e03_matrice.c tiene una matrice come blocco unico sullo
 * heap (calloc di r * c double). Lo stesso blocco si guarda in due modi: piatto,
 * double *, con l'indice i * c + j (trasposta, prodotto, stampa); oppure a righe,
 * double (*m)[c], con m[i][j] (riempi, traccia), dove il VLA sta SOLO nel tipo del
 * parametro e non alloca nulla. Il passaggio fra i due e' un cast: (double (*)[c])m;
 * sizeof *righe e' la misura di una riga, c * 8 byte.
 * Scelte: il prodotto controlla le dimensioni e restituisce NULL se non tornano; il
 * risultato e' sempre un blocco nuovo, mai uno degli operandi (prodotto(a, b, a)
 * sovrascriverebbe a mentre la sta leggendo). Con 2000 x 2000 la matrice e' 32 MB:
 * sullo heap ci sta, un VLA locale double m[r][c] morirebbe di segmentation fault
 * senza messaggio (lo stack e' 8 MB). Il ciclo del prodotto e' in ordine i, k, j
 * perche' scorra la memoria in avanti.
 * La traccia di m * m^T e' confrontata con il valore atteso, la somma di (i + j)^2
 * su tutta la matrice: interi sotto 2^53, quindi esatti anche in double.
 * Prova: ./bin/e03 3 4   e poi   ./bin/e03 2000 2000
 */
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "e03_matrice.h"

static int leggi_dimensione(const char *s, size_t *out) {
    char *fine;
    errno = 0;
    unsigned long v = strtoul(s, &fine, 10);
    if (fine == s || *fine != '\0' || errno == ERANGE || v == 0) return 0;
    *out = (size_t)v;
    return 1;
}

int main(int argc, char *argv[]) {
    size_t r, c;
    if (argc != 3 || !leggi_dimensione(argv[1], &r) || !leggi_dimensione(argv[2], &c)) {
        fprintf(stderr, "uso: %s righe colonne   (interi positivi, es. 3 4)\n", argv[0]);
        return 1;
    }

    double *m = matrice_new(r, c);
    if (m == NULL) {
        fprintf(stderr, "memoria esaurita per %zu x %zu\n", r, c);
        return 1;
    }
    double (*righe)[c] = (double (*)[c])m;      /* la vista a righe dello stesso blocco */
    riempi(r, c, righe);
    printf("matrice %zu x %zu: %zu byte, una riga (sizeof *righe) %zu byte\n",
           r, c, r * c * sizeof *m, sizeof *righe);

    double *t = trasposta(r, c, m);              /* c x r */
    double *p = (t != NULL) ? prodotto(r, c, m, c, r, t) : NULL;   /* r x r */
    if (t == NULL || p == NULL) {
        fprintf(stderr, "memoria esaurita\n");
        matrice_free(p);
        matrice_free(t);
        matrice_free(m);
        return 1;
    }

    if (r <= 6 && c <= 6) {
        printf("m:\n");
        stampa(r, c, m);
        printf("trasposta:\n");
        stampa(c, r, t);
        printf("m * trasposta:\n");
        stampa(r, r, p);
    }

    /* il controllo delle dimensioni: m (r x c) per m (r x c) si puo' solo se c == r */
    if (r != c) {
        double *sbagliato = prodotto(r, c, m, r, c, m);
        printf("prodotto(m, m) con %zu != %zu: %s\n", c, r,
               sbagliato == NULL ? "NULL, come da contratto" : "ERRORE: doveva essere NULL");
        matrice_free(sbagliato);
    }

    double tr = traccia(r, (double (*)[r])p);   /* p e' r x r: la vista a righe di r */
    double attesa = 0;
    for (size_t i = 0; i < r; i++)
        for (size_t j = 0; j < c; j++)
            attesa += (double)((i + j) * (i + j));
    printf("traccia(m * m^T): ottenuta %.0f, attesa %.0f  %s\n", tr, attesa,
           tr == attesa ? "ok" : "DIVERSA");

    matrice_free(p);
    matrice_free(t);
    matrice_free(m);
    p = t = m = NULL;
    return 0;
}
