/* e08_calc.h: il modulo dei calcoli. Incluso due volte da e08.c (direttamente e
 * attraverso e08_util.h): senza la guardia il typedef verrebbe ridefinito e il
 * compilatore si fermerebbe con "typedef redefinition". */
#ifndef E08_CALC_H
#define E08_CALC_H

#include <stddef.h>
#include <stdint.h>

typedef struct {
    int32_t min, max;
    double media;
} Statistiche;

int64_t calc_somma(const int32_t *v, size_t n);
double calc_media(const int32_t *v, size_t n);          /* richiede n > 0 (assert) */
Statistiche calc_statistiche(const int32_t *v, size_t n); /* richiede n > 0 (assert) */

#endif /* E08_CALC_H */
