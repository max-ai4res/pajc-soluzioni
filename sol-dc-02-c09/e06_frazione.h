/* frazione: numeri razionali, sempre ridotti e con denominatore positivo */
#ifndef E06_FRAZIONE_H
#define E06_FRAZIONE_H

#include <stdbool.h>

/* tipo pubblico, piccolo: si passa e si restituisce per valore */
typedef struct { long num, den; } Frazione;

/* num/den ridotta ai minimi termini, con den > 0. Se den e' 0: false, e *f non
   viene toccata. */
bool frazione_new(long num, long den, Frazione *f);

Frazione frazione_somma(Frazione a, Frazione b);
Frazione frazione_prodotto(Frazione a, Frazione b);

/* negativo, zero, positivo: come un comparatore */
int frazione_confronta(Frazione a, Frazione b);

/* scrive "3/4", oppure "3" se il denominatore e' 1 */
void frazione_stampa(Frazione f);

#endif
