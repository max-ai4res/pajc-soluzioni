/*
 * Esercizio 11.5 - Le figure su file
 *
 * Legge un elenco di figure, una per riga:
 *     cerchio 1.5
 *     rettangolo 3 2
 *     triangolo 3 4 5
 * le crea come oggetti con vtable, le ordina per area crescente con qsort, le
 * stampa e le distrugge attraverso la tabella.
 *
 * Due tabelle di puntatori a funzione fanno due lavori diversi. La vtable di
 * ogni tipo (ShapeVT) dice come si calcola e come si distrugge un oggetto:
 * serve DOPO che l'oggetto esiste. La tabella FABBRICHE dice come si
 * COSTRUISCE una figura a partire dal suo nome e dai numeri letti: serve
 * prima, quando l'oggetto non c'e' ancora (e' il pattern Factory). Per
 * aggiungere un tipo: la struct, le sue funzioni, la sua vtable, e una riga in
 * FABBRICHE. Il ciclo di lettura non cambia.
 *
 * Scelte: il vettore delle figure e' un array di Shape * che cresce. Il
 * comparatore riceve gli indirizzi di due elementi, cioe' due Shape **. Le
 * righe sbagliate (nome sconosciuto, numeri mancanti, misure impossibili)
 * vengono segnalate con il loro numero e saltate.
 *
 * Errore tipico evitato: liberare le figure con free. Oggi tutte si liberano
 * cosi', ma chi ha in mano una Shape * non lo sa e non deve saperlo: si
 * chiama shape_free, che passa dalla tabella.
 */
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define PI 3.14159265358979323846

typedef struct Shape Shape;
typedef struct {
    const char *nome;
    double (*area)(const Shape *self);
    void (*distruggi)(Shape *self);
} ShapeVT;
struct Shape { const ShapeVT *vt; };

static const char *shape_nome(const Shape *s) { return s->vt->nome; }
static double shape_area(const Shape *s)      { return s->vt->area(s); }
static void shape_free(Shape *s)              { if (s != NULL) s->vt->distruggi(s); }
static void semplice_distruggi(Shape *self)   { free(self); }

typedef struct { Shape base; double r; } Circle;
static double circle_area(const Shape *self) {
    const Circle *c = (const Circle *)self;
    return PI * c->r * c->r;
}
static const ShapeVT CIRCLE_VT = { "cerchio", circle_area, semplice_distruggi };

typedef struct { Shape base; double w, h; } Rectangle;
static double rectangle_area(const Shape *self) {
    const Rectangle *r = (const Rectangle *)self;
    return r->w * r->h;
}
static const ShapeVT RECTANGLE_VT = { "rettangolo", rectangle_area, semplice_distruggi };

typedef struct { Shape base; double a, b, c; } Triangle;
static double triangle_area(const Shape *self) {
    const Triangle *t = (const Triangle *)self;
    double p = (t->a + t->b + t->c) / 2;
    return sqrt(p * (p - t->a) * (p - t->b) * (p - t->c));
}
static const ShapeVT TRIANGLE_VT = { "triangolo", triangle_area, semplice_distruggi };

/* le fabbriche: da n numeri a una figura; NULL se i numeri non vanno bene */
static Shape *crea_cerchio(const double *v) {
    if (v[0] <= 0) return NULL;
    Circle *c = malloc(sizeof *c);
    if (c == NULL) return NULL;
    *c = (Circle){ { &CIRCLE_VT }, v[0] };
    return &c->base;
}
static Shape *crea_rettangolo(const double *v) {
    if (v[0] <= 0 || v[1] <= 0) return NULL;
    Rectangle *r = malloc(sizeof *r);
    if (r == NULL) return NULL;
    *r = (Rectangle){ { &RECTANGLE_VT }, v[0], v[1] };
    return &r->base;
}
static Shape *crea_triangolo(const double *v) {
    double a = v[0], b = v[1], c = v[2];
    if (a <= 0 || b <= 0 || c <= 0 || a + b <= c || a + c <= b || b + c <= a) return NULL;
    Triangle *t = malloc(sizeof *t);
    if (t == NULL) return NULL;
    *t = (Triangle){ { &TRIANGLE_VT }, a, b, c };
    return &t->base;
}

typedef struct {
    const char *nome;
    int numeri;                          /* quanti numeri vuole */
    Shape *(*crea)(const double *v);
} Fabbrica;

static const Fabbrica FABBRICHE[] = {
    { "cerchio",    1, crea_cerchio    },
    { "rettangolo", 2, crea_rettangolo },
    { "triangolo",  3, crea_triangolo  },
};
#define N_FABBRICHE (sizeof FABBRICHE / sizeof FABBRICHE[0])

/* gli elementi sono Shape *: arrivano due Shape ** */
static int per_area(const void *a, const void *b) {
    double x = shape_area(*(const Shape *const *)a), y = shape_area(*(const Shape *const *)b);
    return (x > y) - (x < y);
}

int main(void) {
    Shape **forme = NULL;
    size_t n = 0, cap = 0;

    char riga[128];
    for (int numero = 1; fgets(riga, sizeof riga, stdin) != NULL; numero++) {
        char nome[32];
        double v[3] = { 0 };
        int letti = sscanf(riga, "%31s %lf %lf %lf", nome, &v[0], &v[1], &v[2]);
        if (letti < 1) continue;                    /* riga vuota */

        const Fabbrica *f = NULL;
        for (size_t i = 0; i < N_FABBRICHE; i++)
            if (strcmp(nome, FABBRICHE[i].nome) == 0) f = &FABBRICHE[i];
        Shape *s = f != NULL && letti - 1 == f->numeri ? f->crea(v) : NULL;
        if (s == NULL) { fprintf(stderr, "riga %d ignorata: %s", numero, riga); continue; }

        if (n == cap) {
            size_t nuova = cap == 0 ? 8 : cap * 2;
            Shape **p = realloc(forme, nuova * sizeof *p);
            if (p == NULL) { shape_free(s); break; }
            forme = p;
            cap = nuova;
        }
        forme[n++] = s;
    }

    qsort(forme, n, sizeof forme[0], per_area);
    double totale = 0;
    for (size_t i = 0; i < n; i++) {
        printf("%-10s %8.2f\n", shape_nome(forme[i]), shape_area(forme[i]));
        totale += shape_area(forme[i]);
    }
    printf("%-10s %8.2f\n", "totale", totale);

    for (size_t i = 0; i < n; i++) shape_free(forme[i]);
    free(forme);
    return 0;
}
