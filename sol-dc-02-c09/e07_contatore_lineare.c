/* contatore, prima implementazione: array non ordinato, ricerca sequenziale */
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

static Voce *cerca(const Contatore *c, const char *parola) {
    for (size_t i = 0; i < c->n; i++)
        if (strcmp(c->v[i].parola, parola) == 0) return &c->v[i];
    return NULL;
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
    c->v[c->n++] = (Voce){ copia, 1 };           /* in fondo: nessun ordine */
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

const char *contatore_implementazione(void) { return "array non ordinato"; }
