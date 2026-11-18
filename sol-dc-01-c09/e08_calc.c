/* e08_calc.c: i calcoli, con assert sui precondizioni e LOG sui passi */
#include <assert.h>

#include "e08_calc.h"
#include "e08_util.h"

int64_t calc_somma(const int32_t *v, size_t n) {
    LOG("calc_somma");
    int64_t somma = 0;
    for (size_t i = 0; i < n; i++) somma += v[i];
    return somma;
}

double calc_media(const int32_t *v, size_t n) {
    assert(n > 0 && "calc_media: vettore vuoto");       /* sparisce con -DNDEBUG */
    LOG("calc_media");
    return (double)calc_somma(v, n) / (double)n;
}

Statistiche calc_statistiche(const int32_t *v, size_t n) {
    assert(n > 0 && "calc_statistiche: vettore vuoto");
    LOG("calc_statistiche");
    Statistiche s = { .min = v[0], .max = v[0], .media = calc_media(v, n) };
    for (size_t i = 1; i < n; i++) {
        if (v[i] < s.min) s.min = v[i];
        if (v[i] > s.max) s.max = v[i];
    }
    return s;
}
