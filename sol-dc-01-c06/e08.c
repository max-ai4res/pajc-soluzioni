/*
 * Esercizio 6.8 - Un vettore dinamico
 *
 * Vettore tiene il blocco v, la lunghezza n e la capacita' cap. push
 * raddoppia la capacita' quando n == cap (da 0 si parte con 1), pop
 * restituisce l'ultimo elemento e dimezza la capacita' quando n scende
 * sotto cap / 4: dimezzare gia' a cap / 2 farebbe riallocare a ogni push e
 * pop alternati sul confine. Ogni realloc passa per una variabile
 * d'appoggio: tmp = realloc(v, ...); if (tmp == NULL) ... altrimenti v =
 * realloc(v, ...) perderebbe il blocco quando realloc fallisce. get controlla
 * l'indice e restituisce false fuori dall'intervallo invece di leggere a
 * caso. Il main inserisce diecimila numeri, ne toglie novemila e stampa
 * capacita' e lunghezza ogni volta che la capacita' cambia.
 */
#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct {
    int *v;
    size_t n, cap;
} Vettore;

void vettore_init(Vettore *vet) {
    vet->v = NULL;
    vet->n = 0;
    vet->cap = 0;
}

/* porta la capacita' a nuova_cap (>= n); false se la memoria manca */
static bool vettore_ridimensiona(Vettore *vet, size_t nuova_cap) {
    int *tmp = realloc(vet->v, nuova_cap * sizeof *tmp);  /* realloc(NULL, ..) = malloc */
    if (tmp == NULL) return false;
    vet->v = tmp;
    vet->cap = nuova_cap;
    return true;
}

/* aggiunge x in coda; raddoppia quando e' pieno; false se la memoria manca */
bool vettore_push(Vettore *vet, int x) {
    if (vet->n == vet->cap) {
        size_t nuova = vet->cap == 0 ? 1 : 2 * vet->cap;
        if (!vettore_ridimensiona(vet, nuova)) return false;
    }
    vet->v[vet->n++] = x;
    return true;
}

/* toglie e restituisce l'ultimo (il vettore non deve essere vuoto);
   dimezza quando n scende sotto un quarto della capacita' */
int vettore_pop(Vettore *vet) {
    assert(vet->n > 0);
    int x = vet->v[--vet->n];
    if (vet->cap > 1 && vet->n < vet->cap / 4)
        vettore_ridimensiona(vet, vet->cap / 2);  /* se fallisce si tiene il blocco grande */
    return x;
}

/* l'elemento i in *out; false se i e' fuori dall'intervallo */
bool vettore_get(const Vettore *vet, size_t i, int *out) {
    if (i >= vet->n) return false;
    *out = vet->v[i];
    return true;
}

void vettore_free(Vettore *vet) {
    free(vet->v);
    vet->v = NULL;
    vet->n = 0;
    vet->cap = 0;
}

int main(void) {
    Vettore vet;
    vettore_init(&vet);
    size_t cap_prec = vet.cap;

    printf("push di 10000 numeri:\n");
    for (int i = 0; i < 10000; i++) {
        if (!vettore_push(&vet, i)) {
            fprintf(stderr, "memoria insufficiente\n");
            vettore_free(&vet);
            return EXIT_FAILURE;
        }
        if (vet.cap != cap_prec) {
            printf("  cap %5zu  n %5zu\n", vet.cap, vet.n);
            cap_prec = vet.cap;
        }
    }

    int x;
    printf("get(5000): %s %d (atteso 5000)\n",
           vettore_get(&vet, 5000, &x) ? "ok" : "fuori", x);
    printf("get(10000): %s (atteso fuori)\n",
           vettore_get(&vet, 10000, &x) ? "ok" : "fuori");

    printf("pop di 9000 numeri:\n");
    long long somma = 0;
    for (int i = 0; i < 9000; i++) {
        somma += vettore_pop(&vet);
        if (vet.cap != cap_prec) {
            printf("  cap %5zu  n %5zu\n", vet.cap, vet.n);
            cap_prec = vet.cap;
        }
    }
    printf("somma dei tolti: %lld (attesa %lld)\n", somma,
           (9999LL * 10000 / 2) - (999LL * 1000 / 2));
    printf("restano n %zu, cap %zu\n", vet.n, vet.cap);

    vettore_free(&vet);
    return 0;
}
