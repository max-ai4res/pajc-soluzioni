/* rubrica: un insieme di contatti ordinato per nome. Tipo opaco. Non conosce
   ne' la tastiera ne' lo schermo. */
#ifndef E09_RUBRICA_H
#define E09_RUBRICA_H

#include <stdbool.h>
#include <stddef.h>

#include "e09_contatto.h"

typedef struct Rubrica Rubrica;

/* una rubrica vuota. E' del chiamante: rubrica_free. NULL se manca memoria. */
Rubrica *rubrica_new(void);
void rubrica_free(Rubrica *r);                      /* NULL non fa nulla */

typedef enum { RUBRICA_OK, RUBRICA_DOPPIO, RUBRICA_MEMORIA } EsitoRubrica;

/* aggiunge una COPIA del contatto, al suo posto in ordine di nome.
   RUBRICA_DOPPIO se il nome c'e' gia' (e non cambia nulla). */
EsitoRubrica rubrica_aggiungi(Rubrica *r, Contatto c);

/* il contatto con quel nome, o NULL. In prestito: vale fino alla prossima
   aggiunta o rimozione. */
const Contatto *rubrica_cerca(const Rubrica *r, const char *nome);

/* toglie il contatto con quel nome; false se non c'e' */
bool rubrica_togli(Rubrica *r, const char *nome);

size_t rubrica_quanti(const Rubrica *r);

/* chiama f(contatto, ctx) per ogni contatto, in ordine di nome */
void rubrica_per_ognuno(const Rubrica *r, void (*f)(const Contatto *c, void *ctx),
                        void *ctx);

#endif
