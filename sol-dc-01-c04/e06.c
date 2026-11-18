/*
 * Esercizio 4.6 - swap e dintorni
 *
 * Tre funzioni che modificano variabili del chiamante attraverso il loro
 * indirizzo: swap(int *, int *) scambia due int; scambia_e_azzera scambia e
 * poi azzera *b (riusa swap); swap_ptr(int **, int **) scambia due PUNTATORI,
 * quindi riceve gli indirizzi dei puntatori, &px e &py.
 *
 * Il main dichiara x, y, px = &x, py = &y, e dopo ogni chiamata stampa valori
 * e indirizzi di tutti e quattro: si vede che swap cambia i valori di x e y ma
 * non i loro indirizzi ne' i puntatori; che swap_ptr non tocca x e y, ma dopo
 * px punta a y e py a x.
 *
 * Errore tipico evitato: swap_ptr(px, py) non compila (int * dove serve
 * int **). Se la firma fosse stata scritta male, swap_ptr(int *a, int *b),
 * compilerebbe e scambierebbe x e y invece dei puntatori: prima di scrivere il
 * corpo si decide che cosa deve cambiare e si passa l'indirizzo DI QUELLO.
 */
#include <stdio.h>

void swap(int *a, int *b) {
    int t = *a;
    *a = *b;
    *b = t;
}

void scambia_e_azzera(int *a, int *b) {
    swap(a, b);
    *b = 0;
}

void swap_ptr(int **a, int **b) {
    int *t = *a;
    *a = *b;
    *b = t;
}

static void stato(const char *momento, const int *x, const int *y,
                  int *const *px, int *const *py) {
    printf("%s\n", momento);
    printf("  x  = %3d  (a %p)\n", *x, (const void *)x);
    printf("  y  = %3d  (a %p)\n", *y, (const void *)y);
    printf("  px = %p -> %d\n", (const void *)*px, **px);
    printf("  py = %p -> %d\n", (const void *)*py, **py);
}

int main(void) {
    int x = 10, y = 20;
    int *px = &x, *py = &y;
    stato("inizio:", &x, &y, &px, &py);

    swap(&x, &y);
    stato("dopo swap(&x, &y): valori scambiati, indirizzi e puntatori uguali",
          &x, &y, &px, &py);

    swap_ptr(&px, &py);
    stato("dopo swap_ptr(&px, &py): x e y intatte, px e py scambiati",
          &x, &y, &px, &py);

    scambia_e_azzera(px, py);
    stato("dopo scambia_e_azzera(px, py): scambiati, poi *py (cioe' x) a zero",
          &x, &y, &px, &py);

    printf("atteso alla fine: x = 0, y = 20, px -> y, py -> x\n");
    return 0;
}
