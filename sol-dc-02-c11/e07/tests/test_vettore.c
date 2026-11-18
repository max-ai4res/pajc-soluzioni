/* prove del modulo vettore */
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "vettore.h"

static int liberati = 0;
static void conta_e_libera(void *e) { liberati++; free(e); }

static int cmp_int_ptr(const void *a, const void *b) {
    int x = **(int *const *)a, y = **(int *const *)b;
    return (x > y) - (x < y);
}

static void somma(void *e, void *ctx) { *(int *)ctx += *(int *)e; }

int main(void) {
    Vettore *vt = vettore_new(conta_e_libera);
    assert(vt != NULL);
    assert(vettore_quanti(vt) == 0);
    assert(vettore_prendi(vt, 0) == NULL);

    for (int i = 0; i < 100; i++) {             /* abbastanza da crescere piu' volte */
        int *x = malloc(sizeof *x);
        assert(x != NULL);
        *x = 99 - i;
        bool ok = vettore_aggiungi(vt, x);
        assert(ok);
    }
    assert(vettore_quanti(vt) == 100);
    assert(*(int *)vettore_prendi(vt, 0) == 99);
    assert(vettore_prendi(vt, 100) == NULL);

    vettore_ordina(vt, cmp_int_ptr);
    assert(*(int *)vettore_prendi(vt, 0) == 0);
    assert(*(int *)vettore_prendi(vt, 99) == 99);

    int totale = 0;
    vettore_per_ognuno(vt, somma, &totale);
    assert(totale == 4950);

    vettore_free(vt);
    assert(liberati == 100);                    /* ogni elemento, una volta sola */
    vettore_free(NULL);                         /* lecito */

    printf("test_vettore: ok\n");
    return 0;
}
