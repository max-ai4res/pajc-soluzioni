/* registro: gli utenti della chat */
#ifndef REGISTRO_H
#define REGISTRO_H

#include <stdbool.h>
#include <stddef.h>

typedef struct Registro Registro;       /* opaco */
typedef struct Utente Utente;           /* opaco */

/* un registro vuoto, ordinato per nome. E' del chiamante: registro_free. */
Registro *registro_new(void);
void registro_free(Registro *r);        /* libera anche gli utenti; NULL non fa nulla */

/* l'utente con quel nome, o NULL. In prestito: vale finche' il registro vive. */
Utente *registro_cerca(const Registro *r, const char *nome);

/* crea un utente con una COPIA del nome (troncato a 16 caratteri) e lo restituisce
   in prestito; NULL se manca la memoria. */
Utente *registro_aggiungi(Registro *r, const char *nome);

size_t registro_quanti(const Registro *r);

/* sceglie come registro_per_ognuno ordina gli utenti: "nome" o "messaggi".
   false se il criterio non esiste, e in quel caso non cambia nulla. */
bool registro_ordina_per(Registro *r, const char *criterio);

/* chiama f(utente, ctx) per ogni utente, nell'ordine scelto */
void registro_per_ognuno(Registro *r, void (*f)(const Utente *u, void *ctx), void *ctx);

const char *utente_nome(const Utente *u);       /* in prestito dall'utente */
int utente_messaggi(const Utente *u);
void utente_ha_scritto(Utente *u);              /* un messaggio in piu' */

#endif
