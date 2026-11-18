/* misure: un elenco di misure con nome, ordinabile per valore. Tipo opaco.
   (versione con i difetti: esercizio 11.6) */
#ifndef E06_BUG_MISURE_H
#define E06_BUG_MISURE_H

#include <stdbool.h>
#include <stddef.h>

typedef struct { char *nome; int valore; } Misura;
typedef struct Misure Misure;

/* un elenco vuoto. E' del chiamante: misure_free. NULL se manca la memoria. */
Misure *misure_new(void);
void misure_free(Misure *m);                    /* NULL non fa nulla */

/* aggiunge una misura con una COPIA del nome (in prestito). false se manca la
   memoria, e in quel caso l'elenco non cambia. */
bool misure_aggiungi(Misure *m, const char *nome, int valore);

size_t misure_quante(const Misure *m);

/* la misura i, o NULL. In prestito: vale fino alla prossima aggiunta. */
const Misura *misure_prendi(const Misure *m, size_t i);

/* ordina per valore crescente e, a parita', per nome */
void misure_ordina(Misure *m);

/* quante misure sono state aggiunte in tutto, in tutti gli elenchi */
size_t misure_totale = 0;

#endif
