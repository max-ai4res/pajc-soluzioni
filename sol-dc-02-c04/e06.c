/*
 * Esercizio 4.6 - scambia, inverti, stampa_array
 *
 * Tre funzioni che non conoscono il tipo degli elementi: ricevono l'indirizzo
 * dell'array come void *, il numero di elementi e la dimensione di ciascuno.
 * inverti scambia il primo con l'ultimo, il secondo con il penultimo, ...,
 * calcolando gli indirizzi a mano (base + i * dim su char *). stampa_array
 * percorre e chiama una callback di stampa, che e' la sola a sapere il tipo.
 *
 * Scelte: le callback di stampa ricevono const void *, l'indirizzo
 * dell'elemento, come un comparatore. Per l'array di stringhe l'elemento e'
 * un char *, quindi la callback riceve un char ** e toglie un livello.
 *
 * Errore tipico evitato: passare come dim la dimensione sbagliata
 * (sizeof(char) per un array di stringhe). Qui e' sempre sizeof v[0].
 */
#include <stdio.h>

void scambia(void *a, void *b, size_t dim) {
    unsigned char *x = a, *y = b;
    for (size_t i = 0; i < dim; i++) {
        unsigned char t = x[i]; x[i] = y[i]; y[i] = t;
    }
}

void inverti(void *base, size_t n, size_t dim) {
    char *v = base;
    for (size_t i = 0; i < n / 2; i++)
        scambia(v + i * dim, v + (n - 1 - i) * dim, dim);
}

void stampa_array(const void *base, size_t n, size_t dim, void (*stampa)(const void *)) {
    const char *v = base;
    for (size_t i = 0; i < n; i++) stampa(v + i * dim);
    printf("\n");
}

static void stampa_int(const void *e)    { printf("%d ", *(const int *)e); }
static void stampa_double(const void *e) { printf("%g ", *(const double *)e); }
static void stampa_str(const void *e)    { printf("%s ", *(const char *const *)e); }

int main(void) {
    int v[] = { 1, 2, 3, 4, 5 };
    double d[] = { 1.5, 2.5, 3.5, 4.5 };
    const char *parole[] = { "uno", "due", "tre" };

    inverti(v, sizeof v / sizeof v[0], sizeof v[0]);
    stampa_array(v, sizeof v / sizeof v[0], sizeof v[0], stampa_int);

    inverti(d, sizeof d / sizeof d[0], sizeof d[0]);
    stampa_array(d, sizeof d / sizeof d[0], sizeof d[0], stampa_double);

    inverti(parole, sizeof parole / sizeof parole[0], sizeof parole[0]);
    stampa_array(parole, sizeof parole / sizeof parole[0], sizeof parole[0], stampa_str);
    return 0;
}
