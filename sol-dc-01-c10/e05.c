/* Esercizio 10.5 - La pila di rettangoli
 * Pila e' un array dinamico di Rectangle con i tre campi (v, n, cap): pila_push
 * raddoppia con realloc e la variabile d'appoggio, pila_pop toglie l'ultimo.
 * Firma di pila_pop: bool pila_pop(Pila *p, Rectangle *out). Un Rectangle non ha un
 * valore "nessuno" da restituire quando la pila e' vuota, e il controllo if (p->n == 0)
 * deve venire prima di tutto (p->v[p->n - 1] con n = 0 e' p->v[SIZE_MAX]); l'esito in
 * un bool si legge nel modo naturale, if (pila_pop(&p, &r)), e chi vuole solo scartare
 * l'ultimo passa NULL. L'alternativa (Rectangle per valore e bool *ok) obbliga a
 * inventare un rettangolo fittizio quando ok e' falso.
 * Le due versioni di "il piu' grande": pila_max_v restituisce una COPIA (32 byte nel
 * frame del chiamante: vive quanto lui), pila_max_p un puntatore DENTRO l'array. Dopo
 * abbastanza push la pila cresce, realloc sposta il blocco e libera il vecchio: la
 * copia e' ancora valida, il puntatore pende (heap-use-after-free con ASan). La lettura
 * del puntatore pendente e' eseguita solo con l'argomento "pendente", cosi' il
 * programma normale e' pulito sotto ASan e la dimostrazione si fa a comando:
 *     make asan && ./bin/e05            (pulito)
 *     ./bin/e05 pendente                (ASan: heap-use-after-free sulla riga segnata)
 * Senza ASan realloc puo' anche non spostare il blocco, e la lettura "funziona" per
 * caso: il programma stampa i due indirizzi per vederlo. La regola: un puntatore dentro
 * un array che cresce vale fino al prossimo push; si tiene l'indice o si cerca di nuovo.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

typedef struct { double x, y, w, h; } Rectangle;

typedef struct {
    Rectangle *v;
    size_t n, cap;
} Pila;

static double rectangle_area(const Rectangle *r) { return r->w * r->h; }

bool pila_init(Pila *p, size_t cap) {
    if (cap == 0) cap = 1;
    p->v = malloc(cap * sizeof *p->v);
    if (p->v == NULL) return false;
    p->n = 0;
    p->cap = cap;
    return true;
}

bool pila_push(Pila *p, Rectangle r) {
    if (p->n == p->cap) {
        size_t nuova = p->cap * 2;
        Rectangle *tmp = realloc(p->v, nuova * sizeof *tmp);
        if (tmp == NULL) return false;            /* p->v e' ancora valido */
        printf("  [cresce: %zu -> %zu, blocco %p -> %p]\n", p->cap, nuova,
               (void *)p->v, (void *)tmp);
        p->v = tmp;
        p->cap = nuova;
    }
    p->v[p->n++] = r;
    return true;
}

/* L'ultimo rettangolo in *out (se out non e' NULL); false se la pila e' vuota. */
bool pila_pop(Pila *p, Rectangle *out) {
    if (p->n == 0) return false;                  /* prima di tutto */
    p->n--;
    if (out != NULL) *out = p->v[p->n];
    return true;
}

double pila_area_totale(const Pila *p) {
    double tot = 0;
    for (size_t i = 0; i < p->n; i++) tot += rectangle_area(&p->v[i]);
    return tot;
}

/* Il rettangolo di area massima, per valore: una copia. Pila vuota: tutto a zero. */
Rectangle pila_max_v(const Pila *p) {
    Rectangle max = {0};
    for (size_t i = 0; i < p->n; i++)
        if (i == 0 || rectangle_area(&p->v[i]) > rectangle_area(&max)) max = p->v[i];
    return max;
}

/* Il rettangolo di area massima, per puntatore: dentro l'array, valido fino al
 * prossimo push. NULL se la pila e' vuota. */
Rectangle *pila_max_p(const Pila *p) {
    if (p->n == 0) return NULL;
    Rectangle *max = &p->v[0];
    for (size_t i = 1; i < p->n; i++)
        if (rectangle_area(&p->v[i]) > rectangle_area(max)) max = &p->v[i];
    return max;
}

void pila_free(Pila *p) {
    free(p->v);
    p->v = NULL;
    p->n = p->cap = 0;
}

static void stampa_rect(const char *nome, const Rectangle *r) {
    printf("%s: %g x %g in (%g, %g), area %g\n", nome, r->w, r->h, r->x, r->y,
           rectangle_area(r));
}

int main(int argc, char *argv[]) {
    bool pendente = argc > 1 && strcmp(argv[1], "pendente") == 0;
    Pila p;
    if (!pila_init(&p, 2)) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    pila_push(&p, (Rectangle){ .w = 3, .h = 2 });
    pila_push(&p, (Rectangle){ .x = 1, .y = 1, .w = 5, .h = 2 });   /* area 10: il massimo */

    Rectangle mv = pila_max_v(&p);                /* una copia nel frame del main */
    Rectangle *mp = pila_max_p(&p);               /* un indirizzo dentro p.v */
    stampa_rect("max per valore   ", &mv);
    stampa_rect("max per puntatore", mp);
    printf("blocco della pila: %p, mp = %p (indice %td)\n", (void *)p.v, (void *)mp, mp - p.v);
    void *vecchio = p.v;                          /* solo per confrontare gli indirizzi */

    printf("sei push: la pila cresce da 2 a 8\n");
    for (int i = 0; i < 6; i++)
        pila_push(&p, (Rectangle){ .w = 1, .h = 1 });
    printf("blocco della pila ora: %p %s\n", (void *)p.v,
           (void *)p.v == vecchio ? "(non si e' mosso: senza ASan puo' capitare)" : "(spostato)");

    stampa_rect("max per valore, dopo i push   ", &mv);     /* la copia vale ancora */
    if (pendente) {
        printf("lettura del puntatore pendente (solo con l'argomento \"pendente\"):\n");
        stampa_rect("max per puntatore, dopo i push", mp);  /* ASan: heap-use-after-free */
    } else {
        printf("max per puntatore, dopo i push: NON si legge, mp punta al blocco vecchio\n"
               "  (./bin/e05 pendente lo legge lo stesso, e ASan lo ferma)\n");
    }
    Rectangle *mp2 = pila_max_p(&p);              /* la forma giusta: si cerca di nuovo */
    stampa_rect("max per puntatore, ricalcolato", mp2);

    printf("area totale: %g (attesa %g)\n", pila_area_totale(&p), 6.0 + 10.0 + 6.0);

    Rectangle r;
    size_t tolti = 0;
    while (pila_pop(&p, &r)) tolti++;
    printf("tolti %zu rettangoli, l'ultimo uscito ha area %g; pila vuota: pop -> %s\n",
           tolti, rectangle_area(&r), pila_pop(&p, &r) ? "true" : "false");

    pila_free(&p);
    return 0;
}
