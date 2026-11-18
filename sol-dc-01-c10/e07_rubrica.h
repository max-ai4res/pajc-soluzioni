/* e07_rubrica.h: la rubrica dinamica (che cosa esiste).
 * Contratto di possesso: la Rubrica possiede l'array v; ogni Contatto possiede il suo
 * nome (allocato in rubrica_aggiungi, liberato in rubrica_rimuovi o rubrica_free).
 * Nessun puntatore a un Contatto o a un nome sopravvive a un rubrica_aggiungi (il
 * realloc puo' spostare l'array) ne' a un rubrica_rimuovi: si tiene l'indice che
 * rubrica_cerca restituisce, e lo si ricalcola dopo ogni modifica. */
#ifndef E07_RUBRICA_H
#define E07_RUBRICA_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include "e07_util.h"

enum { TELEFONO_MAX = 16, EMAIL_MAX = 48 };

/* Un recapito facoltativo: nessuno, un secondo telefono o un'email. */
typedef enum { NESSUNO, TELEFONO, EMAIL } TipoRecapito;

typedef struct {
    TipoRecapito tipo;                    /* il discriminante: quale campo vale */
    union {
        char telefono[TELEFONO_MAX];
        char email[EMAIL_MAX];
    } u;
} Recapito;

enum { FLAG_PREFERITO = 1u << 0 };

typedef struct {
    char *nome;                           /* allocato: del contatto */
    char telefono[TELEFONO_MAX];
    Recapito recapito;
    uint8_t flags;                        /* maschere: FLAG_* */
} Contatto;

typedef struct {
    Contatto *v;                          /* il blocco sullo heap, in ordine di nome */
    size_t n, cap;
} Rubrica;

/* Recapiti: NESSUNO, oppure EMAIL se il testo contiene una '@', altrimenti TELEFONO.
 * Il testo viene tagliato alla misura del campo. */
Recapito recapito_nessuno(void);
Recapito recapito_da_testo(const char *s);
void recapito_stampa(const Recapito *r, FILE *out);

/* Alloca la capacita' iniziale (almeno 1); false se la memoria non basta. */
bool rubrica_init(Rubrica *r, size_t cap);

/* Inserisce in ordine di nome (copia allocata di nome, telefono tagliato a 15
 * caratteri); raddoppia con realloc quando e' piena. false se la memoria non basta:
 * la rubrica resta com'era. */
bool rubrica_aggiungi(Rubrica *r, const char *nome, const char *telefono, Recapito recapito);

/* L'indice del primo contatto con quel nome, o SIZE_MAX se non c'e' (ricerca binaria:
 * la rubrica e' ordinata). Un indice, non un puntatore: vale attraverso un realloc. */
size_t rubrica_cerca(const Rubrica *r, const char *nome);

/* Toglie il primo contatto con quel nome, liberandone il nome e chiudendo il buco con
 * memmove; false se non c'e'. */
bool rubrica_rimuovi(Rubrica *r, const char *nome);

void rubrica_stampa(const Rubrica *r, FILE *out);

/* Libera ogni nome e poi l'array; la rubrica torna vuota (una seconda chiamata e'
 * innocua). */
void rubrica_free(Rubrica *r);

#endif /* E07_RUBRICA_H */
