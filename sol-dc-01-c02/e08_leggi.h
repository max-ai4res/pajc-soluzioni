/* Modulo leggi: lettura di interi da stdin in un array fisso (esercizio 2.8) */
#ifndef E08_LEGGI_H
#define E08_LEGGI_H

#include <stddef.h>

/* Legge interi da stdin finche' ce ne sono (o l'array e' pieno) e li mette
 * in v, che ha spazio per max elementi. Restituisce quanti ne ha letti. */
size_t leggi_interi(int *v, size_t max);

#endif
