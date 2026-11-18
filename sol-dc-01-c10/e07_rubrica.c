/* e07_rubrica.c: la rubrica dinamica (come funziona). */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "e07_rubrica.h"

Recapito recapito_nessuno(void) {
    return (Recapito){ .tipo = NESSUNO };
}

Recapito recapito_da_testo(const char *s) {
    Recapito r;
    if (strchr(s, '@') != NULL) {
        r.tipo = EMAIL;
        snprintf(r.u.email, sizeof r.u.email, "%s", s);
    } else {
        r.tipo = TELEFONO;
        snprintf(r.u.telefono, sizeof r.u.telefono, "%s", s);
    }
    return r;
}

void recapito_stampa(const Recapito *r, FILE *out) {
    switch (r->tipo) {                    /* prima il discriminante, poi il campo */
    case NESSUNO:  break;
    case TELEFONO: fprintf(out, "tel. %s", r->u.telefono); break;
    case EMAIL:    fprintf(out, "%s", r->u.email); break;
    }
}

bool rubrica_init(Rubrica *r, size_t cap) {
    if (cap == 0) cap = 1;
    r->v = malloc(cap * sizeof *r->v);
    if (r->v == NULL) return false;
    r->n = 0;
    r->cap = cap;
    return true;
}

/* Raddoppia il blocco, con la variabile d'appoggio. */
static bool rubrica_cresci(Rubrica *r) {
    size_t nuova = r->cap * 2;
    Contatto *tmp = realloc(r->v, nuova * sizeof *tmp);
    if (tmp == NULL) return false;        /* r->v e' ancora valido */
    char msg[64];
    snprintf(msg, sizeof msg, "rubrica piena, raddoppio %zu -> %zu", r->cap, nuova);
    LOG(msg);
    r->v = tmp;
    r->cap = nuova;
    return true;
}

bool rubrica_aggiungi(Rubrica *r, const char *nome, const char *telefono, Recapito recapito) {
    if (r->n == r->cap && !rubrica_cresci(r)) return false;
    size_t ln = strlen(nome) + 1;
    char *copia = malloc(ln);
    if (copia == NULL) return false;
    memcpy(copia, nome, ln);

    Contatto *c = &r->v[r->n];            /* in coda, poi risale al suo posto */
    c->nome = copia;
    snprintf(c->telefono, sizeof c->telefono, "%s", telefono);
    c->recapito = recapito;
    c->flags = 0;
    r->n++;

    /* inserzione: il nuovo contatto scende finche' il precedente non viene prima
     * (strcmp < 0 stretto: fra nomi uguali il nuovo resta dopo, ordine stabile) */
    for (size_t j = r->n - 1; j > 0 && strcmp(r->v[j].nome, r->v[j - 1].nome) < 0; j--)
        SWAP_T(Contatto, r->v[j], r->v[j - 1]);   /* la struct intera, nome compreso */
    return true;
}

size_t rubrica_cerca(const Rubrica *r, const char *nome) {
    size_t lo = 0, hi = r->n;             /* il primo indice con nome >= cercato */
    while (lo < hi) {
        size_t mid = lo + (hi - lo) / 2;
        if (strcmp(r->v[mid].nome, nome) < 0) lo = mid + 1;
        else hi = mid;
    }
    if (lo < r->n && strcmp(r->v[lo].nome, nome) == 0) return lo;
    return SIZE_MAX;
}

bool rubrica_rimuovi(Rubrica *r, const char *nome) {
    size_t i = rubrica_cerca(r, nome);
    if (i == SIZE_MAX) return false;
    char msg[64];
    snprintf(msg, sizeof msg, "rimozione all'indice %zu di %zu", i, r->n);
    LOG(msg);
    free(r->v[i].nome);                   /* 1. il nome del contatto che esce */
    r->v[i].nome = NULL;
    memmove(&r->v[i], &r->v[i + 1], (r->n - i - 1) * sizeof r->v[0]);   /* 2. il buco */
    r->n--;                               /* 3. il conto */
    return true;
}

void rubrica_stampa(const Rubrica *r, FILE *out) {
    for (size_t i = 0; i < r->n; i++) {
        const Contatto *c = &r->v[i];
        fprintf(out, "%c %-24s %-16s", (c->flags & FLAG_PREFERITO) ? '*' : ' ',
                c->nome, c->telefono);
        recapito_stampa(&c->recapito, out);
        fputc('\n', out);
    }
}

void rubrica_free(Rubrica *r) {
    for (size_t i = 0; i < r->n; i++) {   /* ogni nome... */
        free(r->v[i].nome);
        r->v[i].nome = NULL;
    }
    free(r->v);                           /* ...poi l'array */
    r->v = NULL;
    r->n = r->cap = 0;
}
