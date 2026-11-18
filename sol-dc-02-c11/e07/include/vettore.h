/* vettore: un array di void * che cresce */
#ifndef VETTORE_H
#define VETTORE_H

#include <stdbool.h>
#include <stddef.h>

typedef struct Vettore Vettore;                     /* opaco: i campi sono in vettore.c */

typedef void (*Libera)(void *elemento);
typedef void (*Visita)(void *elemento, void *ctx);
typedef int (*Confronto)(const void *, const void *);

/* un vettore vuoto. Se libera non e' NULL il vettore possiede i suoi elementi e
   li libera con quella funzione. Il vettore e' del chiamante: vettore_free. */
Vettore *vettore_new(Libera libera);
void vettore_free(Vettore *vt);

/* aggiunge in fondo. Se il vettore possiede, da ora e e' suo; se fallisce (false)
   e resta del chiamante. */
bool vettore_aggiungi(Vettore *vt, void *e);

size_t vettore_quanti(const Vettore *vt);

/* l'elemento i, o NULL se non c'e'. In prestito: resta del vettore. */
void *vettore_prendi(const Vettore *vt, size_t i);

/* chiama f(elemento, ctx) per ogni elemento, in ordine */
void vettore_per_ognuno(const Vettore *vt, Visita f, void *ctx);

/* ordina gli elementi; cmp riceve gli indirizzi di due elementi (void **) */
void vettore_ordina(Vettore *vt, Confronto cmp);

#endif
