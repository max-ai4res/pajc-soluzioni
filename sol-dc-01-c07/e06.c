/*
 * Esercizio 7.6 - Punto e Segmento
 *
 * Idea: Segmento contiene due Punto per valore, quindi un segmento e' un blocco di
 * quattro double contigui, senza nulla da liberare. lunghezza e punto_medio ricevono
 * il segmento per puntatore const perche' lo leggono soltanto; trasla per puntatore
 * perche' lo modifica. punto_medio restituisce il Punto per valore con un letterale
 * composto, che e' il "costruttore" del C: la copia finisce nel frame del chiamante.
 * Errore evitato: s->a.x, una freccia per il puntatore e un punto per la struct
 * contenuta. s->a->x non compila (a non e' un puntatore), s.a.x nemmeno (s lo e').
 * sqrt sta in math.h e richiede -lm nel link (LDLIBS nel Makefile).
 */
#include <math.h>
#include <stdio.h>

typedef struct { double x, y; } Punto;
typedef struct { Punto a, b; } Segmento;

double lunghezza(const Segmento *s) {
    double dx = s->b.x - s->a.x, dy = s->b.y - s->a.y;
    return sqrt(dx * dx + dy * dy);
}

Punto punto_medio(const Segmento *s) {
    return (Punto){ .x = (s->a.x + s->b.x) / 2, .y = (s->a.y + s->b.y) / 2 };
}

void trasla(Segmento *s, double dx, double dy) {
    s->a.x += dx;
    s->a.y += dy;
    s->b.x += dx;
    s->b.y += dy;
}

static void stampa_segmento(const char *nome, const Segmento *s) {
    printf("%s: (%g, %g) -> (%g, %g)\n", nome, s->a.x, s->a.y, s->b.x, s->b.y);
}

int main(void) {
    Segmento s = { .a = { 0, 0 }, .b = { 3, 4 } };
    stampa_segmento("segmento", &s);
    printf("lunghezza:   attesa 5, ottenuta %g\n", lunghezza(&s));
    Punto m = punto_medio(&s);
    printf("punto medio: atteso (1.5, 2), ottenuto (%g, %g)\n", m.x, m.y);

    trasla(&s, 1, -1);
    stampa_segmento("traslato di (1, -1)", &s);
    printf("lunghezza:   attesa 5 (la traslazione non la cambia), ottenuta %g\n",
           lunghezza(&s));
    m = punto_medio(&s);
    printf("punto medio: atteso (2.5, 1), ottenuto (%g, %g)\n", m.x, m.y);

    printf("sizeof(Punto) = %zu, sizeof(Segmento) = %zu: due punti dentro, non due puntatori\n",
           sizeof(Punto), sizeof(Segmento));
    return 0;
}
