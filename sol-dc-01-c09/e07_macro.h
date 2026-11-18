/* e07_macro.h: una libreria di macro, con la guardia e le parentesi al posto giusto */
#ifndef E07_MACRO_H
#define E07_MACRO_H

/* quanti elementi ha un array dichiarato nel punto in cui si usa (non un parametro) */
#define ARRAY_LEN(a) (sizeof (a) / sizeof (a)[0])

/* a e b compaiono due volte: niente effetti collaterali negli argomenti */
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define MAX(a, b) ((a) > (b) ? (a) : (b))

/* x compare tre volte, lo e hi due: con MIN(MAX(x, lo), hi) x comparirebbe quattro volte */
#define CLAMP(x, lo, hi) ((x) < (lo) ? (lo) : (x) > (hi) ? (hi) : (x))

/* x compare due volte */
#define ABS(x) ((x) < 0 ? -(x) : (x))

#endif /* E07_MACRO_H */
