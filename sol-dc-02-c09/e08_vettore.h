/* vettore: un array di void * che cresce. Tipo opaco. */
#ifndef E08_VETTORE_H
#define E08_VETTORE_H

#include <stdbool.h>
#include <stddef.h>

typedef struct Vettore Vettore;

typedef void (*Libera)(void *elemento);
typedef void (*Visita)(void *elemento, void *ctx);
typedef int (*Confronto)(const void *, const void *);

/* un vettore vuoto. Se libera non e' NULL il vettore possiede gli elementi e li
   libera con quella funzione. E' del chiamante: vettore_free. */
Vettore *vettore_new(Libera libera);
void vettore_free(Vettore *vt);                     /* NULL non fa nulla */

/* aggiunge in fondo. Se il vettore possiede, da ora e e' suo; se fallisce (false)
   e resta del chiamante. */
bool vettore_aggiungi(Vettore *vt, void *e);

/* toglie l'elemento i, liberandolo se il vettore lo possiede; false se non c'e' */
bool vettore_rimuovi(Vettore *vt, size_t i);

size_t vettore_quanti(const Vettore *vt);

/* l'elemento i, o NULL. In prestito: resta del vettore, e non va usato dopo che
   e' stato rimosso. */
void *vettore_prendi(const Vettore *vt, size_t i);

void vettore_per_ognuno(const Vettore *vt, Visita f, void *ctx);

/* cmp riceve gli indirizzi di due elementi, cioe' due void ** */
void vettore_ordina(Vettore *vt, Confronto cmp);

#endif
