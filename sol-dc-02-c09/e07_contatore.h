/* contatore: quante volte compare ogni parola. Tipo opaco: questo header ha due
   implementazioni intercambiabili (e07_contatore_lineare.c, _ordinato.c). */
#ifndef E07_CONTATORE_H
#define E07_CONTATORE_H

#include <stdbool.h>
#include <stddef.h>

typedef struct Contatore Contatore;

/* un contatore vuoto. E' del chiamante: contatore_free. NULL se manca memoria. */
Contatore *contatore_new(void);
void contatore_free(Contatore *c);              /* NULL non fa nulla */

/* conta un'occorrenza di parola (in prestito: il contatore ne tiene una copia).
   false se manca memoria. */
bool contatore_conta(Contatore *c, const char *parola);

/* quante volte e' stata contata; 0 se mai */
size_t contatore_quante(const Contatore *c, const char *parola);

/* chiama f(parola, quante, ctx) per ogni parola diversa; la parola e' in prestito.
   L'ordine dipende dall'implementazione. */
void contatore_per_ognuno(const Contatore *c,
                          void (*f)(const char *parola, size_t quante, void *ctx),
                          void *ctx);

/* il nome dell'implementazione, per chi vuole stamparlo */
const char *contatore_implementazione(void);

#endif
