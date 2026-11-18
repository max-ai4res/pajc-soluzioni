/*
 * Esercizio 3.9 - Ordinare gli indici
 *
 * L'array dei giocatori non si tocca: si ordinano due array di indici,
 * per_nome[] e per_punti[], in modo che g[per_nome[0]], g[per_nome[1]], ...
 * siano i giocatori in ordine di nome, e lo stesso per i punti. Cosi' dello
 * stesso array esistono due ordinamenti insieme, e chi tiene un puntatore a
 * un giocatore non se lo vede spostare.
 *
 * L'idea: gli elementi che ordina_ctx sposta sono size_t. Il comparatore
 * riceve gli indirizzi di due indici, ma deve confrontare i GIOCATORI a cui
 * quegli indici si riferiscono, e per arrivarci gli serve l'array: e' il
 * contesto. Con qsort, che non ha il parametro ctx, l'array dovrebbe stare in
 * una variabile globale.
 *
 * Errore tipico evitato: confrontare i due indici ricevuti (*a < *b). Ordina
 * gli indici per valore, cioe' non fa nulla: 0, 1, 2, ... sono gia' in ordine.
 */
#include <stdio.h>
#include <string.h>

typedef struct { char nome[16]; int punti; } Giocatore;

typedef int (*ConfrontoCtx)(const void *, const void *, void *ctx);

static void scambia(char *a, char *b, size_t dim) {
    for (size_t i = 0; i < dim; i++) {
        char t = a[i]; a[i] = b[i]; b[i] = t;
    }
}

void ordina_ctx(void *base, size_t n, size_t dim, ConfrontoCtx cmp, void *ctx) {
    char *v = base;
    for (size_t i = 1; i < n; i++)
        for (size_t j = i; j > 0; j--) {
            char *a = v + (j - 1) * dim, *b = v + j * dim;
            if (cmp(a, b, ctx) <= 0) break;
            scambia(a, b, dim);
        }
}

/* a e b sono indirizzi di indici; ctx e' l'array dei giocatori */
static int idx_per_nome(const void *a, const void *b, void *ctx) {
    const Giocatore *g = ctx;
    size_t i = *(const size_t *)a, j = *(const size_t *)b;
    return strcmp(g[i].nome, g[j].nome);
}

static int idx_per_punti(const void *a, const void *b, void *ctx) {
    const Giocatore *g = ctx;
    size_t i = *(const size_t *)a, j = *(const size_t *)b;
    if (g[i].punti != g[j].punti) return (g[i].punti < g[j].punti) - (g[i].punti > g[j].punti);
    return strcmp(g[i].nome, g[j].nome);
}

static void stampa(const char *titolo, const Giocatore *g, const size_t *idx, size_t n) {
    printf("%s:", titolo);
    for (size_t k = 0; k < n; k++) printf(" %s(%d)", g[idx[k]].nome, g[idx[k]].punti);
    printf("\n");
}

int main(void) {
    Giocatore g[] = {
        { "Nami", 30 }, { "Greta", 50 }, { "Max", 30 }, { "Loretta", 10 }, { "Ada", 40 },
    };
    size_t n = sizeof g / sizeof g[0];

    size_t per_nome[sizeof g / sizeof g[0]], per_punti[sizeof g / sizeof g[0]];
    for (size_t i = 0; i < n; i++) per_nome[i] = per_punti[i] = i;

    ordina_ctx(per_nome, n, sizeof per_nome[0], idx_per_nome, g);
    ordina_ctx(per_punti, n, sizeof per_punti[0], idx_per_punti, g);

    stampa("per nome ", g, per_nome, n);
    stampa("per punti", g, per_punti, n);
    printf("l'array  :");
    for (size_t i = 0; i < n; i++) printf(" %s(%d)", g[i].nome, g[i].punti);
    printf("\n");
    return 0;
}
