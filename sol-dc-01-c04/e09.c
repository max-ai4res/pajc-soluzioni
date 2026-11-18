/*
 * Esercizio 4.9 - Trova e azzera
 *
 * int *trova(int *v, size_t n, int x) restituisce l'indirizzo della prima
 * occorrenza di x nei primi n elementi di v, o NULL. Restituire un puntatore
 * dentro l'array (e non un indice) permette al chiamante di modificare la
 * cella trovata direttamente: *p = 0.
 *
 * azzera_tutte usa trova in un ciclo: dopo ogni occorrenza riparte dalla
 * cella successiva, p + 1, con n ridotta di quante celle ha gia' saltato; la
 * differenza p - v (ptrdiff_t, quante celle dall'inizio) dice dove si e'
 * arrivati. azzera_tutte_da fa lo stesso con la variante trova_da(v, n, da, x),
 * che riceve l'indice di partenza e restituisce un indice (o n se non c'e'):
 * piu' semplice da leggere, stesso risultato.
 *
 * Errore tipico evitato: ripartire da p invece che da p + 1 (ciclo infinito,
 * perche' trova restituisce sempre la stessa cella... che pero' nel frattempo
 * e' stata azzerata: con x != 0 il ciclo finirebbe, ma per il motivo
 * sbagliato), e dimenticare di ridurre n: trova leggerebbe oltre la fine
 * dell'array. ASan non ha nulla da dire.
 */
#include <stdio.h>
#include <stddef.h>

int *trova(int *v, size_t n, int x) {
    for (size_t i = 0; i < n; i++)
        if (v[i] == x)
            return &v[i];
    return NULL;
}

/* Variante con l'indice di partenza: restituisce l'indice della prima
 * occorrenza di x in v[da..n), o n se non c'e'. */
size_t trova_da(const int *v, size_t n, size_t da, int x) {
    for (size_t i = da; i < n; i++)
        if (v[i] == x)
            return i;
    return n;
}

/* Azzera tutte le occorrenze di x con trova; restituisce quante erano. */
static size_t azzera_tutte(int *v, size_t n, int x) {
    size_t azzerate = 0;
    int *p = trova(v, n, x);
    while (p != NULL) {
        *p = 0;
        azzerate++;
        size_t gia_viste = (size_t)(p - v) + 1;   /* celle fino a p inclusa */
        p = trova(p + 1, n - gia_viste, x);
    }
    return azzerate;
}

/* Lo stesso con trova_da e gli indici. */
static size_t azzera_tutte_da(int *v, size_t n, int x) {
    size_t azzerate = 0;
    for (size_t i = trova_da(v, n, 0, x); i < n; i = trova_da(v, n, i + 1, x)) {
        v[i] = 0;
        azzerate++;
    }
    return azzerate;
}

static void stampa(const char *nome, const int *v, size_t n) {
    printf("%-12s", nome);
    for (size_t i = 0; i < n; i++)
        printf(" %d", v[i]);
    printf("\n");
}

int main(void) {
    int v[] = {7, 3, 7, 1, 7, 7, 2, 7};
    size_t n = sizeof v / sizeof v[0];
    stampa("v:", v, n);

    int *p = trova(v, n, 3);
    printf("trova(v, n, 3) -> indice %td (atteso 1); trova(v, n, 9) -> %s\n",
           p - v, trova(v, n, 9) == NULL ? "NULL" : "non NULL");

    size_t k = azzera_tutte(v, n, 7);
    printf("azzera_tutte(v, n, 7): %zu occorrenze (attese 5)\n", k);
    stampa("dopo:", v, n);

    int w[] = {7, 3, 7, 1, 7, 7, 2, 7};
    k = azzera_tutte_da(w, n, 7);
    printf("azzera_tutte_da(w, n, 7): %zu occorrenze (attese 5)\n", k);
    stampa("dopo:", w, n);

    /* l'ultima cella e' un'occorrenza: la ripartenza da p + 1 con n ridotta a
     * zero deve restituire NULL senza leggere oltre l'array */
    int z[] = {5, 5};
    k = azzera_tutte(z, 2, 5);
    printf("azzera_tutte(z, 2, 5): %zu occorrenze (attese 2)\n", k);
    stampa("dopo:", z, 2);
    return 0;
}
