/*
 * Esercizio 4.7 - Il massimo di qualunque cosa
 *
 * massimo() restituisce l'INDIRIZZO dell'elemento piu' grande secondo il
 * comparatore, o NULL se l'array e' vuoto. Non puo' restituire "l'elemento":
 * non ne conosce il tipo. Chi chiama, che lo conosce, assegna il void *
 * ricevuto al puntatore giusto e legge.
 *
 * Scelte: la firma ricalca bsearch (base, n, dim, cmp). Il parametro base e'
 * const void *, ma il risultato e' void * senza const, come fanno bsearch e
 * strchr: cosi' chi ha un array modificabile puo' modificare l'elemento
 * trovato senza cast. A parita' restituisce il primo.
 *
 * Errore tipico evitato: per l'array di stringhe l'indirizzo restituito e'
 * quello di un elemento, cioe' di un char *: va assegnato a un const char **
 * e dereferenziato una volta. Assegnarlo a un const char * stamperebbe i byte
 * del puntatore.
 */
#include <stdio.h>
#include <string.h>

void *massimo(const void *base, size_t n, size_t dim,
              int (*cmp)(const void *, const void *)) {
    if (n == 0) return NULL;
    const char *v = base, *max = v;
    for (size_t i = 1; i < n; i++)
        if (cmp(v + i * dim, max) > 0) max = v + i * dim;
    return (void *)max;
}

typedef struct { double x, y, w, h; } Rectangle;

static int cmp_int(const void *a, const void *b) {
    int x = *(const int *)a, y = *(const int *)b;
    return (x > y) - (x < y);
}
static int cmp_double(const void *a, const void *b) {
    double x = *(const double *)a, y = *(const double *)b;
    return (x > y) - (x < y);
}
static int cmp_str(const void *a, const void *b) {
    return strcmp(*(const char *const *)a, *(const char *const *)b);
}
static int cmp_area(const void *a, const void *b) {
    const Rectangle *r = a, *s = b;
    double x = r->w * r->h, y = s->w * s->h;
    return (x > y) - (x < y);
}

int main(void) {
    int v[] = { 42, 7, 99, 3 };
    double d[] = { -2.5, -0.5, -9.0 };
    const char *parole[] = { "pera", "uva", "fico" };
    Rectangle r[] = { { 0, 0, 2, 3 }, { 1, 1, 5, 4 }, { 0, 0, 1, 10 } };

    int *mi = massimo(v, 4, sizeof v[0], cmp_int);
    double *md = massimo(d, 3, sizeof d[0], cmp_double);
    const char **ms = massimo(parole, 3, sizeof parole[0], cmp_str);
    Rectangle *mr = massimo(r, 3, sizeof r[0], cmp_area);

    printf("int: %d (indice %td)\n", *mi, mi - v);
    printf("double: %g\n", *md);
    printf("stringa: %s\n", *ms);
    printf("rettangolo: %g x %g, area %g\n", mr->w, mr->h, mr->w * mr->h);
    printf("array vuoto: %s\n", massimo(v, 0, sizeof v[0], cmp_int) == NULL ? "NULL" : "?");
    return 0;
}
