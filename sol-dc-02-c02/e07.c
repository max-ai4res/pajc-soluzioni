/*
 * Esercizio 2.7 - mappa, filtra, riduci
 *
 * Le tre funzioni generali per array di double: mappa trasforma ogni elemento
 * sul posto, filtra compatta l'array tenendo gli elementi che superano la
 * prova e restituisce quanti sono, riduci li combina tutti partendo da un
 * valore iniziale. Nessuna delle tre sa che cosa fanno le callback.
 *
 * Il main legge i numeri da tastiera (fino a fine input o al primo non
 * numero), ne tiene una copia, e calcola:
 *   - la somma delle radici dei positivi: filtra(positivo), mappa(radice),
 *     riduci(somma, 0);
 *   - il massimo delle meta': mappa(meta), riduci(massimo, primo elemento).
 *
 * Errore tipico evitato: riduci(massimo) con valore iniziale 0. Se i numeri
 * sono tutti negativi il "massimo" risulterebbe 0, che nell'array non c'e'.
 * Il valore iniziale giusto per il massimo e' un elemento dell'array (qui il
 * primo), e con l'array vuoto il massimo non esiste: lo si dice. riduci su un
 * array vuoto restituisce il valore iniziale senza chiamare f.
 */
#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#define MAX 100

void mappa(double *v, size_t n, double (*f)(double)) {
    for (size_t i = 0; i < n; i++) v[i] = f(v[i]);
}

size_t filtra(double *v, size_t n, bool (*tieni)(double)) {
    size_t k = 0;
    for (size_t i = 0; i < n; i++)
        if (tieni(v[i])) v[k++] = v[i];
    return k;
}

double riduci(const double *v, size_t n, double iniziale,
              double (*f)(double, double)) {
    double acc = iniziale;
    for (size_t i = 0; i < n; i++) acc = f(acc, v[i]);
    return acc;
}

static double meta(double x)               { return x / 2; }
static double radice(double x)             { return sqrt(x); }
static bool   positivo(double x)           { return x > 0; }
static double somma(double a, double b)    { return a + b; }
static double massimo(double a, double b)  { return a > b ? a : b; }

int main(void) {
    double v[MAX], w[MAX];
    size_t n = 0;
    printf("numeri (fine input per terminare): ");
    while (n < MAX && scanf("%lf", &v[n]) == 1) n++;

    memcpy(w, v, n * sizeof v[0]);          /* la prima catena modifica l'array */
    size_t np = filtra(w, n, positivo);
    mappa(w, np, radice);
    printf("somma delle radici dei positivi: %g\n", riduci(w, np, 0.0, somma));

    memcpy(w, v, n * sizeof v[0]);
    mappa(w, n, meta);
    if (n == 0) printf("massimo delle meta': non esiste (nessun numero)\n");
    else printf("massimo delle meta': %g\n", riduci(w + 1, n - 1, w[0], massimo));
    return 0;
}
