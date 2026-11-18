/*
 * Esercizio 9.7 - Una libreria di macro
 *
 * Idea: le macro stanno in e07_macro.h con la guardia E07_MACRO_H; ognuna ha ogni
 * parametro fra parentesi e tutta la macro fra parentesi, cosi' le espressioni passate
 * come argomento (a + 1, b * 2, x - y) restano intere e il risultato si puo' moltiplicare
 * da fuori (2 * ABS(-3)). Il main confronta valore atteso e ottenuto.
 *
 * Quali macro valutano un argomento piu' di una volta:
 *   ARRAY_LEN  no: sizeof non valuta l'operando (a meno che sia un VLA).
 *   MIN, MAX   si: a e b compaiono due volte ciascuno (nel confronto e nel ramo).
 *   CLAMP      si: x tre volte, lo e hi due volte; scritta come MIN(MAX(x, lo), hi)
 *              x comparirebbe quattro volte. E' il caso in cui una funzione
 *              static inline vince senza discussione.
 *   ABS        si: x due volte.
 * Regola: mai i++ o una chiamata di funzione come argomento di MIN, MAX, CLAMP, ABS.
 * Il main lo mostra con MAX(i++, 2) e i = 4: i sale di due (una volta nel confronto,
 * una nel ramo scelto) e il valore e' 5, che non era in nessuno dei due argomenti.
 */
#include <stdio.h>

#include "e07_macro.h"
#include "e07_macro.h"            /* la seconda inclusione: la guardia la rende innocua */

static void prova_int(const char *espressione, int atteso, int ottenuto) {
    printf("  %-24s atteso %4d, ottenuto %4d  %s\n", espressione, atteso, ottenuto,
           atteso == ottenuto ? "ok" : "ERRORE");
}

static void prova_double(const char *espressione, double atteso, double ottenuto) {
    printf("  %-24s atteso %4g, ottenuto %4g  %s\n", espressione, atteso, ottenuto,
           atteso == ottenuto ? "ok" : "ERRORE");
}

int main(void) {
    int a = 5, b = 2, x = 15, y = 2;
    int v[7] = { 0 };
    double d = -2.5;

    printf("Argomenti semplici:\n");
    prova_int("MIN(3, 8)", 3, MIN(3, 8));
    prova_int("MAX(3, 8)", 8, MAX(3, 8));
    prova_int("CLAMP(5, 0, 10)", 5, CLAMP(5, 0, 10));
    prova_int("CLAMP(-4, 0, 10)", 0, CLAMP(-4, 0, 10));
    prova_int("CLAMP(42, 0, 10)", 10, CLAMP(42, 0, 10));
    prova_int("ABS(-3)", 3, ABS(-3));
    prova_int("ARRAY_LEN(v)", 7, (int)ARRAY_LEN(v));

    printf("Espressioni come argomenti (le parentesi interne):\n");
    prova_int("MIN(a + 1, b * 2)", 4, MIN(a + 1, b * 2));          /* min(6, 4) */
    prova_int("MAX(a + 1, b * 2)", 6, MAX(a + 1, b * 2));          /* max(6, 4) */
    prova_int("CLAMP(x - y, 0, 10)", 10, CLAMP(x - y, 0, 10));     /* 13 -> 10 */
    prova_int("CLAMP(y - x, 0, 10)", 0, CLAMP(y - x, 0, 10));      /* -13 -> 0 */
    prova_int("ABS(y - x)", 13, ABS(y - x));
    prova_double("ABS(d)", 2.5, ABS(d));                           /* funziona anche sui double */

    printf("Macro dentro espressioni (le parentesi esterne):\n");
    prova_int("2 * ABS(-3)", 6, 2 * ABS(-3));
    prova_int("100 / MAX(2, 5)", 20, 100 / MAX(2, 5));
    prova_int("MIN(a, b) * 10", 20, MIN(a, b) * 10);
    prova_int("-CLAMP(x, 0, 10)", -10, -CLAMP(x, 0, 10));

    printf("La doppia valutazione:\n");
    int i = 4;
    int m = MAX(i++, 2);
    printf("  MAX(i++, 2) con i = 4: m = %d, i = %d (ci si aspetterebbe m = 4, i = 5: "
           "i++ e' stato valutato due volte)\n", m, i);
    return 0;
}
