/* contatore, seconda implementazione: array tenuto ordinato, ricerca con bsearch */
#include "e07_contatore.h"

#include <stdlib.h>
#include <string.h>

typedef struct { char *parola; size_t quante; } Voce;

struct Contatore { Voce *v; size_t n, cap; };

static char *str_dup(const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = malloc(n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

/* per bsearch: la chiave e' la parola, l'elemento una Voce */
static int cmp_chiave(const void *chiave, const void *elemento) {
    return strcmp(chiave, ((const Voce *)elemento)->parola);
}

static Voce *cerca(const Contatore *c, const char *parola) {
    return bsearch(parola, c->v, c->n, sizeof c->v[0], cmp_chiave);
}

Contatore *contatore_new(void) { return calloc(1, sizeof(Contatore)); }

void contatore_free(Contatore *c) {
    if (c == NULL) return;
    for (size_t i = 0; i < c->n; i++) free(c->v[i].parola);
    free(c->v);
    free(c);
}

bool contatore_conta(Contatore *c, const char *parola) {
    Voce *voce = cerca(c, parola);
    if (voce != NULL) { voce->quante++; return true; }

    if (c->n == c->cap) {
        size_t cap = c->cap == 0 ? 8 : c->cap * 2;
        Voce *v = realloc(c->v, cap * sizeof *v);
        if (v == NULL) return false;
        c->v = v;
        c->cap = cap;
    }
    char *copia = str_dup(parola);
    if (copia == NULL) return false;

    size_t i = c->n;                             /* il posto che tiene l'ordine */
    while (i > 0 && strcmp(c->v[i - 1].parola, parola) > 0) i--;
    memmove(&c->v[i + 1], &c->v[i], (c->n - i) * sizeof c->v[0]);
    c->v[i] = (Voce){ copia, 1 };
    c->n++;
    return true;
}

size_t contatore_quante(const Contatore *c, const char *parola) {
    const Voce *voce = cerca(c, parola);
    return voce == NULL ? 0 : voce->quante;
}

void contatore_per_ognuno(const Contatore *c,
                          void (*f)(const char *parola, size_t quante, void *ctx),
                          void *ctx) {
    for (size_t i = 0; i < c->n; i++) f(c->v[i].parola, c->v[i].quante, ctx);
}

const char *contatore_implementazione(void) { return "array ordinato e bsearch"; }
