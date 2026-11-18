/* sessione: riceve le righe di testo e le esegue */
#ifndef SESSIONE_H
#define SESSIONE_H

#include <stdbool.h>

#include "uscita.h"

typedef struct Sessione Sessione;       /* opaco */

/* una sessione che risponde su u. u e' in prestito: resta del chiamante, che la
   chiude dopo aver liberato la sessione. NULL se manca la memoria. */
Sessione *sessione_new(Uscita *u);
void sessione_free(Sessione *s);        /* NULL non fa nulla */

/* esegue una riga: un comando se comincia con '/', altrimenti un messaggio.
   La riga e' in prestito e non viene modificata. */
void sessione_elabora(Sessione *s, const char *riga);

/* vero dopo /esci */
bool sessione_finita(const Sessione *s);

#endif
