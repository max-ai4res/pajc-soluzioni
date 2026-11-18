/*
 * Esercizio 7.9 - Il poligono
 *
 * Idea: Poligono e' l'ArrayList di Punto scritto a mano: un blocco sullo heap, quanti
 * vertici ci sono (n) e quanti ce ne stanno (cap). poligono_aggiungi raddoppia con
 * realloc attraverso una variabile d'appoggio, cosi' se il realloc fallisce il blocco
 * vecchio resta valido. Perimetro e area sono somme sui lati, e il lato dall'ultimo
 * vertice al primo chiude il poligono: l'indice del successivo e' (i + 1) % n.
 * L'area usa la formula di Gauss (shoelace): meta' del valore assoluto della somma di
 * x_i * y_{i+1} - x_{i+1} * y_i; il segno dipende dal verso dei vertici, fabs lo toglie.
 * Scelte: cap = 0 nell'init viene portato a 1, perche' il raddoppio di zero resta zero;
 * perimetro con meno di due vertici e area con meno di tre valgono zero.
 * Errore evitato: dimenticare il lato di chiusura (ciclo fino a n - 1 con i + 1 e
 * basta) da' un perimetro aperto e un'area sbagliata.
 */
#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct { double x, y; } Punto;

typedef struct {
    Punto *vertici;    /* il blocco sullo heap: del poligono */
    size_t n, cap;
} Poligono;

bool poligono_init(Poligono *p, size_t cap) {
    if (cap == 0) cap = 1;
    p->vertici = malloc(cap * sizeof *p->vertici);
    if (p->vertici == NULL) {
        p->n = p->cap = 0;
        return false;
    }
    p->n = 0;
    p->cap = cap;
    return true;
}

bool poligono_aggiungi(Poligono *p, Punto v) {
    if (p->n == p->cap) {                               /* pieno: raddoppia */
        size_t nuova = p->cap * 2;
        Punto *tmp = realloc(p->vertici, nuova * sizeof *tmp);
        if (tmp == NULL) return false;                  /* p->vertici e' ancora valido */
        printf("  [cresce: %zu -> %zu vertici]\n", p->cap, nuova);
        p->vertici = tmp;
        p->cap = nuova;
    }
    p->vertici[p->n++] = v;
    return true;
}

static double distanza(Punto a, Punto b) {
    double dx = b.x - a.x, dy = b.y - a.y;
    return sqrt(dx * dx + dy * dy);
}

double poligono_perimetro(const Poligono *p) {
    if (p->n < 2) return 0;
    double somma = 0;
    for (size_t i = 0; i < p->n; i++) {
        size_t j = (i + 1) % p->n;                      /* l'ultimo lato torna al primo */
        somma += distanza(p->vertici[i], p->vertici[j]);
    }
    return somma;
}

double poligono_area(const Poligono *p) {
    if (p->n < 3) return 0;
    double somma = 0;
    for (size_t i = 0; i < p->n; i++) {
        size_t j = (i + 1) % p->n;
        somma += p->vertici[i].x * p->vertici[j].y - p->vertici[j].x * p->vertici[i].y;
    }
    return fabs(somma) / 2;
}

void poligono_free(Poligono *p) {
    free(p->vertici);
    p->vertici = NULL;
    p->n = p->cap = 0;
}

static void poligono_stampa(const char *nome, const Poligono *p,
                            double perimetro_atteso, double area_attesa) {
    printf("%s (%zu vertici):", nome, p->n);
    for (size_t i = 0; i < p->n; i++)
        printf(" (%g, %g)", p->vertici[i].x, p->vertici[i].y);
    printf("\n  perimetro: atteso %g, ottenuto %g\n", perimetro_atteso, poligono_perimetro(p));
    printf("  area:      attesa %g, ottenuta %g\n", area_attesa, poligono_area(p));
}

int main(void) {
    Poligono quadrato;
    if (!poligono_init(&quadrato, 2)) {                 /* cap 2: raddoppia a 4 */
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    Punto q[] = { { 0, 0 }, { 2, 0 }, { 2, 2 }, { 0, 2 } };
    for (size_t i = 0; i < sizeof q / sizeof q[0]; i++)
        if (!poligono_aggiungi(&quadrato, q[i])) {
            fprintf(stderr, "memoria esaurita\n");
            poligono_free(&quadrato);
            return 1;
        }
    poligono_stampa("Quadrato di lato 2", &quadrato, 8, 4);

    Poligono triangolo;
    if (!poligono_init(&triangolo, 1)) {                /* cap 1: raddoppia a 2 e a 4 */
        fprintf(stderr, "memoria esaurita\n");
        poligono_free(&quadrato);
        return 1;
    }
    Punto t[] = { { 0, 0 }, { 3, 0 }, { 0, 4 } };       /* lati 3, 4, 5 */
    for (size_t i = 0; i < sizeof t / sizeof t[0]; i++)
        if (!poligono_aggiungi(&triangolo, t[i])) {
            fprintf(stderr, "memoria esaurita\n");
            poligono_free(&quadrato);
            poligono_free(&triangolo);
            return 1;
        }
    poligono_stampa("Triangolo 3-4-5", &triangolo, 12, 6);

    poligono_free(&quadrato);
    poligono_free(&triangolo);
    printf("liberati: vertici %p e %p, n = %zu e %zu\n", (void *)quadrato.vertici,
           (void *)triangolo.vertici, quadrato.n, triangolo.n);
    return 0;
}
