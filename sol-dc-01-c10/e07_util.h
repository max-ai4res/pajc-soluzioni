/* e07_util.h: le macro comuni della rubrica. La guardia evita la doppia inclusione
 * (e07.c include questo header direttamente e attraverso e07_rubrica.h). */
#ifndef E07_UTIL_H
#define E07_UTIL_H

#include <stdio.h>

/* Quanti elementi ha un array: SOLO dove l'array e' dichiarato, mai su un parametro
 * (che e' un puntatore: darebbe 8 / sizeof elemento). */
#define ARRAY_LEN(a) (sizeof (a) / sizeof (a)[0])

/* Scambia a e b di tipo T; una sola istruzione, sta sotto un if senza graffe. */
#define SWAP_T(T, a, b) do { T tmp_ = (a); (a) = (b); (b) = tmp_; } while (0)

/* Con -DDEBUG stampa file, riga e messaggio su stderr; altrimenti sparisce.
 * Dentro solo testo, mai espressioni con effetti collaterali. */
#ifdef DEBUG
#define LOG(msg) fprintf(stderr, "[%s:%d] %s\n", __FILE__, __LINE__, msg)
#else
#define LOG(msg) ((void)0)
#endif

#endif /* E07_UTIL_H */
