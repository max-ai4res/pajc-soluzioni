/*
 * Esercizio 7.7 - Il rettangolo del bimondo
 *
 * Idea: la classe Java Rectangle diventa una struct per lo stato e funzioni libere
 * con il prefisso del tipo (rectangle_*) e la struct come primo parametro, il this
 * esplicito. rectangle_new restituisce per valore (32 byte, nessun malloc);
 * rectangle_area, rectangle_perimeter e rectangle_contains ricevono const Rectangle *
 * perche' leggono soltanto; rectangle_translate riceve Rectangle * perche' modifica.
 * Il Punto, 16 byte, viaggia per valore: e' piccolo e si legge soltanto.
 * rectangle_contains considera il bordo dentro al rettangolo (intervallo chiuso).
 * Errore evitato: rs[i] e' una struct, non un puntatore, quindi si passa &rs[i] alle
 * funzioni e si usa il punto per i campi; dentro le funzioni r e' un puntatore e vuole
 * la freccia.
 */
#include <stdbool.h>
#include <stdio.h>

typedef struct { double x, y; } Punto;
typedef struct { double x, y, w, h; } Rectangle;

Rectangle rectangle_new(double x, double y, double w, double h) {
    return (Rectangle){ .x = x, .y = y, .w = w, .h = h };
}

double rectangle_area(const Rectangle *r) { return r->w * r->h; }

double rectangle_perimeter(const Rectangle *r) { return 2 * (r->w + r->h); }

void rectangle_translate(Rectangle *r, double dx, double dy) {
    r->x += dx;
    r->y += dy;
}

bool rectangle_contains(const Rectangle *r, Punto p) {
    return p.x >= r->x && p.x <= r->x + r->w &&
           p.y >= r->y && p.y <= r->y + r->h;
}

static void rectangle_stampa(const Rectangle *r) {
    printf("  (%5.1f, %5.1f) %4.1f x %4.1f  area %5.1f  perimetro %5.1f",
           r->x, r->y, r->w, r->h, rectangle_area(r), rectangle_perimeter(r));
}

static void stampa_tutti(const char *titolo, const Rectangle *rs, size_t n) {
    Punto origine = { 0, 0 };
    printf("%s\n", titolo);
    for (size_t i = 0; i < n; i++) {
        rectangle_stampa(&rs[i]);
        printf("  contiene l'origine: %s\n", rectangle_contains(&rs[i], origine) ? "si" : "no");
    }
}

int main(void) {
    Rectangle rs[] = {
        rectangle_new(1, 1, 2, 2),        /* non contiene l'origine */
        rectangle_new(3, 3, 1, 1),        /* non la contiene */
        rectangle_new(-5, -5, 10, 10),    /* la contiene */
    };
    size_t n = sizeof rs / sizeof rs[0];

    stampa_tutti("Tre rettangoli (attesi: no, no, si):", rs, n);

    for (size_t i = 0; i < n; i++) rectangle_translate(&rs[i], -2, -2);
    stampa_tutti("Dopo la traslazione di (-2, -2) (attesi: si, no, si):", rs, n);

    printf("sizeof(Rectangle) = %zu: quattro double, una copia per ogni return\n",
           sizeof(Rectangle));
    return 0;
}
