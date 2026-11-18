/*
 * Esercizio 9.7 - Un contatore, due implementazioni
 *
 * e07_contatore.h dichiara un tipo opaco, Contatore, che conta le occorrenze
 * delle parole. Ha due implementazioni, in due sorgenti: un array non
 * ordinato con ricerca sequenziale (e07_contatore_lineare.c) e un array
 * tenuto ordinato con bsearch (e07_contatore_ordinato.c). Questo main non
 * cambia di una riga: il Makefile lo collega con l'una o con l'altra e
 * produce bin/e07_lineare e bin/e07_ordinato.
 *
 * E' cio' che il tipo opaco permette: il main conosce un puntatore e sei
 * prototipi, quindi la rappresentazione puo' cambiare del tutto senza che lui
 * venga nemmeno ricompilato in modo diverso. L'unica differenza visibile e'
 * l'ordine in cui per_ognuno elenca le parole, e infatti il contratto
 * nell'header dice che l'ordine dipende dall'implementazione: un programma
 * che ci contasse dipenderebbe da cio' che l'interfaccia non promette.
 *
 * Scelte: per_ognuno riceve una callback con il contesto; qui il contesto e'
 * un accumulatore che somma le occorrenze e conta le parole diverse.
 *
 * Errore tipico evitato: collegare i due sorgenti nello stesso eseguibile.
 * Definiscono le stesse funzioni: il linker risponderebbe "duplicate symbol".
 * Un eseguibile, una implementazione.
 */
#include <stdio.h>

#include "e07_contatore.h"

typedef struct { size_t diverse, totale; } Somma;

static void stampa(const char *parola, size_t quante, void *ctx) {
    Somma *s = ctx;
    printf("  %-10s %zu\n", parola, quante);
    s->diverse++;
    s->totale += quante;
}

int main(void) {
    Contatore *c = contatore_new();
    if (c == NULL) return 1;

    char parola[64];
    while (scanf("%63s", parola) == 1)
        if (!contatore_conta(c, parola)) { contatore_free(c); return 1; }

    printf("implementazione: %s\n", contatore_implementazione());
    Somma s = { 0, 0 };
    contatore_per_ognuno(c, stampa, &s);
    printf("%zu parole diverse, %zu in tutto; \"la\" compare %zu volte, \"zebra\" %zu\n",
           s.diverse, s.totale, contatore_quante(c, "la"), contatore_quante(c, "zebra"));
    contatore_free(c);
    return 0;
}
