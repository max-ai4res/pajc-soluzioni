/*
 * Esercizio 10.9 - Tre varianti che convivono
 *
 * Lo stesso progetto del problema 10.7 con un Makefile in cui ogni variante
 * ha le sue cartelle: build/debug/, build/release/, build/asan/, ciascuna con
 * obj/ e bin/. Passare da una variante all'altra non richiede piu' make
 * clean, e ricompila solo cio' che e' cambiato dall'ultima volta IN QUELLA
 * variante.
 *
 *     make                    come make VARIANTE=debug
 *     make VARIANTE=release   in build/release/, con -O2 -DNDEBUG
 *     make VARIANTE=asan test in build/asan/, con AddressSanitizer
 *     make VARIANTE=pippo     errore: variante sconosciuta
 *     make tutte              le tre varianti, una dopo l'altra
 *
 * Come: la variabile VARIANTE entra nei percorsi (B = build/$(VARIANTE)), e
 * le opzioni di ogni variante stanno in variabili dal nome composto,
 * CFLAGS_debug, CFLAGS_release, CFLAGS_asan, scelte con
 * $(CFLAGS_$(VARIANTE)). Una variante sconosciuta da' una variabile vuota...
 * e compilerebbe senza opzioni in build/pippo/: per questo c'e' un controllo
 * esplicito con $(filter) e $(error).
 *
 * Errore tipico evitato: le prove in release. Con -DNDEBUG gli assert
 * spariscono e le prove passerebbero sempre senza controllare nulla. Qui gli
 * oggetti delle prove si compilano con CFLAGS_PROVE, da cui -DNDEBUG e'
 * tolto: in release i moduli sono ottimizzati e senza assert, le prove li
 * usano con gli assert accesi.
 */
#include <stdio.h>

#include "comandi.h"
#include "rubrica.h"

int main(void) {
    Rubrica *r = rubrica_new();
    if (r == NULL) return 1;

    char riga[128];
    printf("rubrica (aiuto per i comandi)\n");
    while (fgets(riga, sizeof riga, stdin) != NULL)
        if (!comandi_esegui(r, riga)) break;

    rubrica_free(r);
    return 0;
}
