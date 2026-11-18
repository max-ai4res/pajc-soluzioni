/*
 * Esercizio 3.8 - ordina_ctx al lavoro
 *
 * ordina_ctx e' l'ordinamento per inserzione generico del capitolo, in cui il
 * comparatore riceve un terzo argomento, il contesto. Qui il contesto e' una
 * struct Ordine che dice la chiave (nome o punti), il verso, e conta i
 * confronti. Il programma genera N giocatori con punteggi casuali, li ordina
 * nei quattro modi e stampa i confronti di ciascuno; poi ordina lo stesso
 * array con qsort e un comparatore che conta in una variabile globale.
 *
 * Scelte: un solo comparatore, confronta(), per i quattro ordinamenti: cio'
 * che cambia sta nel contesto. Ogni ordinamento parte da una copia dei dati
 * iniziali, altrimenti i successivi lavorerebbero su un array gia' ordinato e
 * i numeri non sarebbero confrontabili. Il seme di rand() e' fisso, cosi'
 * l'output e' ripetibile.
 *
 * Errore tipico evitato: dimenticare di azzerare il contatore globale prima
 * di qsort. Il contatore nel contesto nasce a zero con la struct e vale per
 * un ordinamento solo; quello globale si porta dietro i conti precedenti.
 * (L'inserzione fa circa n*n/4 confronti, qsort circa n*log2(n): si vede.)
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define N 1000

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

typedef enum { PER_NOME, PER_PUNTI } Chiave;
typedef struct { Chiave chiave; bool decrescente; long confronti; } Ordine;

static int confronta(const void *a, const void *b, void *ctx) {
    const Giocatore *x = a, *y = b;
    Ordine *o = ctx;
    o->confronti++;
    int r = o->chiave == PER_NOME ? strcmp(x->nome, y->nome)
                                  : (x->punti > y->punti) - (x->punti < y->punti);
    return o->decrescente ? -r : r;
}

static long confronti_globali = 0;
static int per_punti_conta(const void *a, const void *b) {
    const Giocatore *x = a, *y = b;
    confronti_globali++;
    return (x->punti > y->punti) - (x->punti < y->punti);
}

int main(void) {
    static Giocatore iniziali[N], g[N];
    srand(27);
    for (size_t i = 0; i < N; i++) {
        snprintf(iniziali[i].nome, sizeof iniziali[i].nome, "g%04d", rand() % 10000);
        iniziali[i].punti = rand() % 1000;
    }

    const char *nomi[] = { "nome", "punti" };
    for (int c = PER_NOME; c <= PER_PUNTI; c++)
        for (int d = 0; d <= 1; d++) {
            memcpy(g, iniziali, sizeof g);
            Ordine o = { .chiave = (Chiave)c, .decrescente = d, .confronti = 0 };
            ordina_ctx(g, N, sizeof g[0], confronta, &o);
            printf("ordina_ctx per %-5s %-11s: %7ld confronti (primo: %s %d)\n",
                   nomi[c], d ? "decrescente" : "crescente", o.confronti,
                   g[0].nome, g[0].punti);
        }

    memcpy(g, iniziali, sizeof g);
    confronti_globali = 0;                      /* a mano: e' il difetto */
    qsort(g, N, sizeof g[0], per_punti_conta);
    printf("qsort      per punti crescente  : %7ld confronti (primo: %s %d)\n",
           confronti_globali, g[0].nome, g[0].punti);
    return 0;
}
