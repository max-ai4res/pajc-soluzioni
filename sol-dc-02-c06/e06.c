/*
 * Esercizio 6.6 - Tre figure
 *
 * L'interfaccia Shape del capitolo (ShapeVT: nome, area, perimetro,
 * distruggi) con tre tipi: Circle, Rectangle, Triangle. Il main crea un array
 * di figure miste, stampa nome, area e perimetro di ciascuna, l'area totale e
 * la figura di area maggiore, poi le distrugge attraverso la tabella.
 *
 * Scelte: ogni tipo e' una struct che comincia con `Shape base`, ha le sue
 * funzioni static e una tabella static const; il resto del programma usa solo
 * shape_nome/area/perimetro/free. Aggiungere Triangle non ha richiesto di
 * toccare nulla di cio' che c'era. L'area del triangolo e' la formula di
 * Erone: sqrt(p(p-a)(p-b)(p-c)) con p il semiperimetro.
 *
 * Errore tipico evitato: triangle_new(1, 2, 10) non e' un triangolo (un lato
 * e' piu' lungo della somma degli altri due) e sotto la radice ci sarebbe un
 * numero negativo. Il costruttore rifiuta restituendo NULL, e il main
 * controlla ogni figura creata, come per la memoria esaurita.
 */
#include <math.h>
#include <stdio.h>
#include <stdlib.h>

#define PI 3.14159265358979323846

typedef struct Shape Shape;

typedef struct {
    const char *nome;
    double (*area)(const Shape *self);
    double (*perimetro)(const Shape *self);
    void (*distruggi)(Shape *self);
} ShapeVT;

struct Shape { const ShapeVT *vt; };

const char *shape_nome(const Shape *s)  { return s->vt->nome; }
double shape_area(const Shape *s)       { return s->vt->area(s); }
double shape_perimetro(const Shape *s)  { return s->vt->perimetro(s); }
void shape_free(Shape *s)               { if (s != NULL) s->vt->distruggi(s); }

static void semplice_distruggi(Shape *self) { free(self); }

/* --- Circle --- */
typedef struct { Shape base; double r; } Circle;

static double circle_area(const Shape *self) {
    const Circle *c = (const Circle *)self;
    return PI * c->r * c->r;
}
static double circle_perimetro(const Shape *self) {
    const Circle *c = (const Circle *)self;
    return 2 * PI * c->r;
}
static const ShapeVT CIRCLE_VT = { "cerchio", circle_area, circle_perimetro, semplice_distruggi };

Shape *circle_new(double r) {
    if (r <= 0) return NULL;
    Circle *c = malloc(sizeof *c);
    if (c == NULL) return NULL;
    c->base.vt = &CIRCLE_VT;
    c->r = r;
    return &c->base;
}

/* --- Rectangle --- */
typedef struct { Shape base; double w, h; } Rectangle;

static double rectangle_area(const Shape *self) {
    const Rectangle *r = (const Rectangle *)self;
    return r->w * r->h;
}
static double rectangle_perimetro(const Shape *self) {
    const Rectangle *r = (const Rectangle *)self;
    return 2 * (r->w + r->h);
}
static const ShapeVT RECTANGLE_VT = { "rettangolo", rectangle_area, rectangle_perimetro, semplice_distruggi };

Shape *rectangle_new(double w, double h) {
    if (w <= 0 || h <= 0) return NULL;
    Rectangle *r = malloc(sizeof *r);
    if (r == NULL) return NULL;
    r->base.vt = &RECTANGLE_VT;
    r->w = w;
    r->h = h;
    return &r->base;
}

/* --- Triangle --- */
typedef struct { Shape base; double a, b, c; } Triangle;

static double triangle_perimetro(const Shape *self) {
    const Triangle *t = (const Triangle *)self;
    return t->a + t->b + t->c;
}
static double triangle_area(const Shape *self) {
    const Triangle *t = (const Triangle *)self;
    double p = triangle_perimetro(self) / 2;
    return sqrt(p * (p - t->a) * (p - t->b) * (p - t->c));
}
static const ShapeVT TRIANGLE_VT = { "triangolo", triangle_area, triangle_perimetro, semplice_distruggi };

/* NULL se i tre lati non formano un triangolo */
Shape *triangle_new(double a, double b, double c) {
    if (a <= 0 || b <= 0 || c <= 0 || a + b <= c || a + c <= b || b + c <= a) return NULL;
    Triangle *t = malloc(sizeof *t);
    if (t == NULL) return NULL;
    t->base.vt = &TRIANGLE_VT;
    t->a = a;
    t->b = b;
    t->c = c;
    return &t->base;
}

int main(void) {
    Shape *forme[] = {
        circle_new(1.0), rectangle_new(3.0, 2.0), triangle_new(3.0, 4.0, 5.0),
        triangle_new(1.0, 2.0, 10.0),           /* non e' un triangolo: NULL */
        circle_new(0.5),
    };
    size_t n = sizeof forme / sizeof forme[0];

    double totale = 0;
    const Shape *massima = NULL;
    for (size_t i = 0; i < n; i++) {
        const Shape *s = forme[i];
        if (s == NULL) { printf("figura %zu: rifiutata dal costruttore\n", i); continue; }
        printf("%-10s area %6.2f  perimetro %6.2f\n", shape_nome(s), shape_area(s),
               shape_perimetro(s));
        totale += shape_area(s);
        if (massima == NULL || shape_area(s) > shape_area(massima)) massima = s;
    }
    printf("area totale %.2f\n", totale);
    if (massima != NULL) printf("la piu' grande: %s\n", shape_nome(massima));

    for (size_t i = 0; i < n; i++) shape_free(forme[i]);     /* shape_free(NULL) e' lecito */
    return 0;
}
