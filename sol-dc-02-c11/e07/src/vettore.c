/* vettore: un array di void * che cresce */
#include "vettore.h"

#include <stdlib.h>

struct Vettore {
    void **v;            /* i puntatori agli elementi */
    size_t n, cap;       /* quanti sono, quanti ce ne stanno */
    Libera libera;       /* come si libera un elemento; NULL: non sono miei */
};

Vettore *vettore_new(Libera libera) {
    Vettore *vt = malloc(sizeof *vt);
    if (vt == NULL) return NULL;
    *vt = (Vettore){ .v = NULL, .n = 0, .cap = 0, .libera = libera };
    return vt;
}

void vettore_free(Vettore *vt) {
    if (vt == NULL) return;
    if (vt->libera != NULL)
        for (size_t i = 0; i < vt->n; i++) vt->libera(vt->v[i]);
    free(vt->v);
    free(vt);
}

/* fa posto per un elemento in piu'; false se manca la memoria */
static bool cresci(Vettore *vt) {
    if (vt->n < vt->cap) return true;
    size_t cap = vt->cap == 0 ? 4 : vt->cap * 2;
    void **v = realloc(vt->v, cap * sizeof *v);
    if (v == NULL) return false;
    vt->v = v;
    vt->cap = cap;
    return true;
}

bool vettore_aggiungi(Vettore *vt, void *e) {
    if (!cresci(vt)) return false;
    vt->v[vt->n++] = e;
    return true;
}

size_t vettore_quanti(const Vettore *vt) { return vt->n; }

void *vettore_prendi(const Vettore *vt, size_t i) {
    return i < vt->n ? vt->v[i] : NULL;
}

void vettore_per_ognuno(const Vettore *vt, Visita f, void *ctx) {
    for (size_t i = 0; i < vt->n; i++) f(vt->v[i], ctx);
}

void vettore_ordina(Vettore *vt, Confronto cmp) {
    qsort(vt->v, vt->n, sizeof vt->v[0], cmp);
}
