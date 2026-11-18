/* rubrica: un array di Contatto tenuto ordinato per nome */
#include "rubrica.h"

#include <stdlib.h>
#include <string.h>

struct Rubrica { Contatto *v; size_t n, cap; };

/* per bsearch: la chiave e' un nome */
static int cmp_nome(const void *chiave, const void *elemento) {
    return strcmp(chiave, ((const Contatto *)elemento)->nome);
}

Rubrica *rubrica_new(void) { return calloc(1, sizeof(Rubrica)); }

void rubrica_free(Rubrica *r) {
    if (r == NULL) return;
    free(r->v);
    free(r);
}

const Contatto *rubrica_cerca(const Rubrica *r, const char *nome) {
    return bsearch(nome, r->v, r->n, sizeof r->v[0], cmp_nome);
}

EsitoRubrica rubrica_aggiungi(Rubrica *r, Contatto c) {
    if (rubrica_cerca(r, c.nome) != NULL) return RUBRICA_DOPPIO;
    if (r->n == r->cap) {
        size_t cap = r->cap == 0 ? 8 : r->cap * 2;
        Contatto *v = realloc(r->v, cap * sizeof *v);
        if (v == NULL) return RUBRICA_MEMORIA;
        r->v = v;
        r->cap = cap;
    }
    size_t i = r->n;
    while (i > 0 && contatto_confronta(&r->v[i - 1], &c) > 0) i--;
    memmove(&r->v[i + 1], &r->v[i], (r->n - i) * sizeof r->v[0]);
    r->v[i] = c;
    r->n++;
    return RUBRICA_OK;
}

bool rubrica_togli(Rubrica *r, const char *nome) {
    const Contatto *c = rubrica_cerca(r, nome);
    if (c == NULL) return false;
    size_t i = (size_t)(c - r->v);
    memmove(&r->v[i], &r->v[i + 1], (r->n - i - 1) * sizeof r->v[0]);
#ifndef ERRORE_VOLUTO                               /* make rotto: l'errore di proposito */
    r->n--;
#endif
    return true;
}

size_t rubrica_quanti(const Rubrica *r) { return r->n; }

void rubrica_per_ognuno(const Rubrica *r, void (*f)(const Contatto *c, void *ctx),
                        void *ctx) {
    for (size_t i = 0; i < r->n; i++) f(&r->v[i], ctx);
}
