/*
 * Esercizio 10.6 - Dare le cartelle a un programma
 *
 * La rubrica a moduli del capitolo 9 (contatto, rubrica, comandi, main) con la
 * struttura di un progetto: i sorgenti in src/, gli header in include/, gli
 * oggetti in obj/ e l'eseguibile in bin/ (queste due le crea make, e si
 * possono cancellare: make clean).
 *
 * Il Makefile non nomina nessun sorgente: li trova con wildcard, ricava gli
 * oggetti con patsubst, li compila con una regola a modello
 * (obj/%.o: src/%.c | obj) e fa scrivere al compilatore, con -MMD -MP, le
 * dipendenze dagli header nei file .d di obj/, che poi include.
 *
 * La verifica chiesta dall'enunciato (make verifica):
 *     make                      costruisce tutto
 *     touch include/rubrica.h   l'header "cambia"
 *     make                      ricompila rubrica.c, comandi.c, main.c: i tre
 *                               sorgenti che lo includono, e NON contatto.c
 *
 * Errore tipico evitato: un `include` dei file .d senza trattino e senza wildcard.
 * Alla prima compilazione i .d non esistono ancora, e make si fermerebbe
 * cercando un file che si chiama, letteralmente, asterisco punto d.
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
