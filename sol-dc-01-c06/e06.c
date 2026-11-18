/*
 * Esercizio 6.6 - Un array grande quanto serve
 *
 * leggi_interi alloca n int con malloc(n * sizeof *v) (byte, non elementi),
 * controlla NULL, li legge con scanf e restituisce il blocco; se l'input
 * finisce prima lo libera e restituisce NULL, cosi' il chiamante non riceve
 * mai un array mezzo pieno. somma e massimo ricevono const int * e n.
 *
 * Errore evitato: la free NON sta in leggi_interi (restituirebbe un
 * puntatore pendente) e non manca del tutto (leak): sta nel main, che e'
 * chi ha chiesto l'array; il commento sopra la funzione lo dice.
 * Input da stdin: n e poi n interi, anche da pipe (printf '3 1 2 3\n' | ./bin/e06).
 */
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

/* alloca e legge n interi da stdin; il CHIAMANTE libera con free.
   NULL se la memoria manca o l'input finisce prima (e allora non c'e' nulla
   da liberare). */
int *leggi_interi(size_t n) {
    int *v = malloc(n * sizeof *v);
    if (v == NULL) return NULL;
    for (size_t i = 0; i < n; i++) {
        if (scanf("%d", &v[i]) != 1) {
            fprintf(stderr, "mi aspettavo %zu interi, letti %zu\n", n, i);
            free(v);
            return NULL;
        }
    }
    return v;
}

int somma(const int *v, size_t n) {
    int s = 0;
    for (size_t i = 0; i < n; i++) s += v[i];
    return s;
}

/* il massimo di n >= 1 elementi */
int massimo(const int *v, size_t n) {
    int m = INT_MIN;
    for (size_t i = 0; i < n; i++)
        if (v[i] > m) m = v[i];
    return m;
}

int main(void) {
    printf("quanti interi? ");
    fflush(stdout);
    unsigned long n;
    if (scanf("%lu", &n) != 1) {
        fprintf(stderr, "numero non valido\n");
        return EXIT_FAILURE;
    }
    if (n == 0) {
        printf("nessun numero: somma 0, massimo non definito\n");
        return 0;
    }
    printf("%lu interi: ", n);
    fflush(stdout);
    int *v = leggi_interi((size_t)n);
    if (v == NULL) return EXIT_FAILURE;

    printf("letti %lu interi\n", n);
    printf("somma:   %d\n", somma(v, (size_t)n));
    printf("massimo: %d\n", massimo(v, (size_t)n));

    free(v);          /* qui, nel chiamante */
    v = NULL;
    return 0;
}
