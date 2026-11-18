/*
 * Esercizio 6.9 - Un iteratore
 *
 * L'interfaccia Iteratore di interi (ha_prossimo, prossimo, distruggi) con tre
 * implementazioni: su un array (che non possiede), su un intervallo [da, a)
 * con un passo, e un filtro che avvolge un altro iteratore e lascia passare
 * solo gli elementi per cui un predicato con contesto e' vero. somma()
 * consuma un iteratore qualunque senza sapere che cosa c'e' sotto.
 *
 * Scelte: il filtro POSSIEDE l'iteratore interno e lo distrugge con se'. Per
 * poter rispondere a ha_prossimo deve aver gia' trovato il prossimo elemento
 * buono: lo cerca in anticipo (alla creazione e dopo ogni prossimo) e lo
 * tiene da parte in due campi, `pronto` e `valore`. Il predicato riceve un
 * void *ctx, come ogni callback: qui e' il divisore.
 *
 * Errore tipico evitato: distruggere sia il filtro sia l'iteratore che gli e'
 * stato consegnato (doppio free), o solo l'interno (il filtro resta con un
 * puntatore pendente). Dopo filtro_new l'interno non e' piu' di chi l'ha
 * creato: si distrugge solo l'iteratore piu' esterno. Verificato con ASan.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct Iteratore Iteratore;

typedef struct {
    bool (*ha_prossimo)(Iteratore *self);
    int (*prossimo)(Iteratore *self);
    void (*distruggi)(Iteratore *self);
} IteratoreVT;

struct Iteratore { const IteratoreVT *vt; };

bool it_ha_prossimo(Iteratore *it) { return it->vt->ha_prossimo(it); }
int it_prossimo(Iteratore *it)     { return it->vt->prossimo(it); }
void it_free(Iteratore *it)        { if (it != NULL) it->vt->distruggi(it); }

static void semplice_distruggi(Iteratore *self) { free(self); }

/* --- su un array: l'array resta di chi lo ha passato --- */
typedef struct { Iteratore base; const int *v; size_t n, i; } ItArray;

static bool array_ha_prossimo(Iteratore *self) {
    ItArray *a = (ItArray *)self;
    return a->i < a->n;
}
static int array_prossimo(Iteratore *self) {
    ItArray *a = (ItArray *)self;
    return a->v[a->i++];
}
static const IteratoreVT ARRAY_VT = { array_ha_prossimo, array_prossimo, semplice_distruggi };

Iteratore *it_array(const int *v, size_t n) {
    ItArray *a = malloc(sizeof *a);
    if (a == NULL) return NULL;
    *a = (ItArray){ .base = { &ARRAY_VT }, .v = v, .n = n, .i = 0 };
    return &a->base;
}

/* --- su un intervallo [da, a) con passo positivo --- */
typedef struct { Iteratore base; int corrente, fine, passo; } ItIntervallo;

static bool intervallo_ha_prossimo(Iteratore *self) {
    ItIntervallo *r = (ItIntervallo *)self;
    return r->corrente < r->fine;
}
static int intervallo_prossimo(Iteratore *self) {
    ItIntervallo *r = (ItIntervallo *)self;
    int x = r->corrente;
    r->corrente += r->passo;
    return x;
}
static const IteratoreVT INTERVALLO_VT = {
    intervallo_ha_prossimo, intervallo_prossimo, semplice_distruggi,
};

Iteratore *it_intervallo(int da, int a, int passo) {
    if (passo <= 0) return NULL;
    ItIntervallo *r = malloc(sizeof *r);
    if (r == NULL) return NULL;
    *r = (ItIntervallo){ .base = { &INTERVALLO_VT }, .corrente = da, .fine = a, .passo = passo };
    return &r->base;
}

/* --- filtro: avvolge un iteratore, che possiede --- */
typedef bool (*Predicato)(int x, void *ctx);

typedef struct {
    Iteratore base;
    Iteratore *interno;
    Predicato tieni;
    void *ctx;
    bool pronto;         /* c'e' un elemento buono gia' trovato */
    int valore;          /* quale */
} ItFiltro;

/* avanza l'interno fino al prossimo elemento che supera il predicato */
static void filtro_cerca(ItFiltro *f) {
    f->pronto = false;
    while (it_ha_prossimo(f->interno)) {
        int x = it_prossimo(f->interno);
        if (f->tieni(x, f->ctx)) {
            f->valore = x;
            f->pronto = true;
            return;
        }
    }
}

static bool filtro_ha_prossimo(Iteratore *self) { return ((ItFiltro *)self)->pronto; }

static int filtro_prossimo(Iteratore *self) {
    ItFiltro *f = (ItFiltro *)self;
    int x = f->valore;
    filtro_cerca(f);
    return x;
}

static void filtro_distruggi(Iteratore *self) {
    ItFiltro *f = (ItFiltro *)self;
    it_free(f->interno);
    free(f);
}
static const IteratoreVT FILTRO_VT = { filtro_ha_prossimo, filtro_prossimo, filtro_distruggi };

/* prende possesso di `interno`; se fallisce lo distrugge e restituisce NULL */
Iteratore *it_filtro(Iteratore *interno, Predicato tieni, void *ctx) {
    if (interno == NULL) return NULL;
    ItFiltro *f = malloc(sizeof *f);
    if (f == NULL) { it_free(interno); return NULL; }
    *f = (ItFiltro){ .base = { &FILTRO_VT }, .interno = interno, .tieni = tieni, .ctx = ctx };
    filtro_cerca(f);
    return &f->base;
}

/* consuma un iteratore qualunque */
long somma(Iteratore *it) {
    long totale = 0;
    while (it_ha_prossimo(it)) totale += it_prossimo(it);
    return totale;
}

static bool multiplo_di(int x, void *ctx) { return x % *(const int *)ctx == 0; }

int main(void) {
    int tre = 3, due = 2;

    Iteratore *a = it_filtro(it_intervallo(0, 101, 1), multiplo_di, &tre);
    if (a == NULL) return 1;
    printf("multipli di 3 fra 0 e 100: %ld\n", somma(a));
    it_free(a);                                 /* distrugge anche l'intervallo */

    int v[] = { 7, 4, 10, 3, 8, 1 };
    Iteratore *b = it_filtro(it_array(v, sizeof v / sizeof v[0]), multiplo_di, &due);
    if (b == NULL) return 1;
    printf("pari dell'array: %ld\n", somma(b));
    it_free(b);

    /* un filtro sopra un filtro: i multipli di 6 */
    Iteratore *c = it_filtro(it_filtro(it_intervallo(0, 31, 1), multiplo_di, &due),
                             multiplo_di, &tre);
    if (c == NULL) return 1;
    printf("multipli di 6 fra 0 e 30: %ld\n", somma(c));
    it_free(c);
    return 0;
}
