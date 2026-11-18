/*
 * Esercizio 10.7 - Due prove e un bersaglio
 *
 * Il progetto del problema 10.6 con una cartella tests/: test_contatto.c e
 * test_rubrica.c sono due programmi fatti di assert, uno per modulo. `make
 * test` li costruisce (ciascuno e' il suo oggetto piu' tutti i moduli tranne
 * il main del programma) e li esegue in fila, fermandosi al primo che
 * fallisce.
 *
 * Che cosa succede quando una prova fallisce: `make rotto` compila la rubrica
 * con -DERRORE_VOLUTO, che fa sbagliare di proposito rubrica_togli (non
 * decrementa il numero dei contatti), ed esegue le prove. test_rubrica si
 * ferma al primo assert falso, stampando l'espressione, il file e la riga:
 *
 *     Assertion failed: (rubrica_quanti(r) == 2), function main,
 *     file tests/test_rubrica.c, line ...
 *
 * e make si ferma con un errore, senza eseguire le prove successive.
 *
 * Scelte: nelle prove cio' che agisce sta FUORI dagli assert (bool ok =
 * rubrica_togli(...); assert(ok);), cosi' la prova fa le stesse cose anche se
 * compilata con NDEBUG. La callback che raccoglie i nomi mostra come si
 * verifica un per_ognuno: si accumula in una stringa e si confronta.
 *
 * Errore tipico evitato: collegare una prova con TUTTI gli oggetti, compreso
 * obj/main.o. Ci sarebbero due main: "duplicate symbol _main". MODULI e'
 * l'elenco degli oggetti senza quello del main.
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
