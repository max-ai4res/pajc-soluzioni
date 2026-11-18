/* registro: gli utenti della chat */
#include "registro.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "vettore.h"

struct Utente { char nome[17]; int messaggi; };

/* un modo di ordinare gli utenti: il nome per sceglierlo e il comparatore */
typedef struct {
    const char *nome;
    Confronto confronta;
} Criterio;

struct Registro {
    Vettore *utenti;             /* di Utente *, li possiede */
    const Criterio *criterio;    /* l'ordine in uso */
};

/* gli elementi del vettore sono Utente *: il comparatore riceve due Utente ** */
static int per_nome(const void *a, const void *b) {
    const Utente *x = *(const Utente *const *)a, *y = *(const Utente *const *)b;
    return strcmp(x->nome, y->nome);
}

static int per_messaggi(const void *a, const void *b) {
    const Utente *x = *(const Utente *const *)a, *y = *(const Utente *const *)b;
    if (x->messaggi != y->messaggi)
        return (x->messaggi < y->messaggi) - (x->messaggi > y->messaggi);
    return strcmp(x->nome, y->nome);
}

static const Criterio CRITERI[] = {
    { "nome",     per_nome     },
    { "messaggi", per_messaggi },
};
#define N_CRITERI (sizeof CRITERI / sizeof CRITERI[0])

Registro *registro_new(void) {
    Registro *r = malloc(sizeof *r);
    if (r == NULL) return NULL;
    r->utenti = vettore_new(free);
    r->criterio = &CRITERI[0];
    if (r->utenti == NULL) { free(r); return NULL; }
    return r;
}

void registro_free(Registro *r) {
    if (r == NULL) return;
    vettore_free(r->utenti);
    free(r);
}

Utente *registro_cerca(const Registro *r, const char *nome) {
    for (size_t i = 0; i < vettore_quanti(r->utenti); i++) {
        Utente *u = vettore_prendi(r->utenti, i);
        if (strcmp(u->nome, nome) == 0) return u;
    }
    return NULL;
}

Utente *registro_aggiungi(Registro *r, const char *nome) {
    Utente *u = malloc(sizeof *u);
    if (u == NULL || !vettore_aggiungi(r->utenti, u)) {
        free(u);
        return NULL;
    }
    snprintf(u->nome, sizeof u->nome, "%s", nome);
    u->messaggi = 0;
    return u;
}

size_t registro_quanti(const Registro *r) { return vettore_quanti(r->utenti); }

bool registro_ordina_per(Registro *r, const char *criterio) {
    for (size_t i = 0; i < N_CRITERI; i++)
        if (strcmp(criterio, CRITERI[i].nome) == 0) {
            r->criterio = &CRITERI[i];
            return true;
        }
    return false;
}

void registro_per_ognuno(Registro *r, void (*f)(const Utente *u, void *ctx), void *ctx) {
    vettore_ordina(r->utenti, r->criterio->confronta);
    for (size_t i = 0; i < vettore_quanti(r->utenti); i++)
        f(vettore_prendi(r->utenti, i), ctx);
}

const char *utente_nome(const Utente *u) { return u->nome; }
int utente_messaggi(const Utente *u)     { return u->messaggi; }
void utente_ha_scritto(Utente *u)        { u->messaggi++; }
