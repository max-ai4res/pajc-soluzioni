/* misure: un elenco di misure con nome, ordinabile per valore */
#include "e06_misure.h"

#include <stdlib.h>
#include <string.h>

struct Misure { Misura *v; size_t n, cap; };

static size_t totale = 0;               /* del modulo: static, e definita QUI */

Misure *misure_new(void) { return calloc(1, sizeof(Misure)); }

void misure_free(Misure *m) {
    if (m == NULL) return;
    for (size_t i = 0; i < m->n; i++) free(m->v[i].nome);
    free(m->v);
    free(m);
}

bool misure_aggiungi(Misure *m, const char *nome, int valore) {
    if (m->n == m->cap) {
        size_t cap = m->cap == 0 ? 4 : m->cap * 2;
        Misura *v = realloc(m->v, cap * sizeof *v);
        if (v == NULL) return false;
        m->v = v;
        m->cap = cap;
    }
    size_t lung = strlen(nome) + 1;
    char *copia = malloc(lung);                 /* la copia promessa dal contratto */
    if (copia == NULL) return false;
    memcpy(copia, nome, lung);
    m->v[m->n++] = (Misura){ copia, valore };
    totale++;
    return true;
}

size_t misure_quante(const Misure *m) { return m->n; }

const Misura *misure_prendi(const Misure *m, size_t i) {
    return i < m->n ? &m->v[i] : NULL;
}

static int per_valore(const void *a, const void *b) {
    const Misura *x = a, *y = b;
    if (x->valore != y->valore) return (x->valore > y->valore) - (x->valore < y->valore);
    return strcmp(x->nome, y->nome);
}

void misure_ordina(Misure *m) { qsort(m->v, m->n, sizeof m->v[0], per_valore); }

size_t misure_totale(void) { return totale; }
