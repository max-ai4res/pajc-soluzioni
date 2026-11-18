/*
 * Esercizio 11.6 - Il debugging: IL PROGRAMMA CON I DIFETTI
 *
 * Legge da un file righe "nome valore", le ordina per valore e stampa un
 * rapporto. Compila senza avvisi con -Wall -Wextra -Werror, e contiene sei
 * difetti, fra questo file, e06_bug_misure.c/.h ed e06_bug_testo.c/.h.
 * Trovarli e correggerli, uno alla volta: make bug, poi
 *     ./bin/e06_bug e06_misure.txt
 * La versione corretta, con il diario, e' e06.c.
 */
#include <assert.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "e06_bug_misure.h"
#include "e06_bug_testo.h"

int main(int argc, char *argv[]) {
    FILE *f = argc > 1 ? fopen(argv[1], "r") : stdin;
    if (f == NULL) { perror(argv[1]); return 1; }

    Misure *m = misure_new();
    if (m == NULL) { if (f != stdin) fclose(f); return 1; }

    const char *titolo = testo_temp("Rapporto di %s", "Greta");

    int esito = 0;
    char nome[32];
    int valore;
    while (fscanf(f, "%31s %d", nome, &valore) == 2) {
        char *copia = malloc(strlen(nome) + 1);
        if (copia == NULL) { esito = 1; break; }
        strcpy(copia, nome);
        assert(misure_aggiungi(m, copia, valore));
        free(copia);                            /* l'elenco ne ha fatta una copia */
    }
    if (f != stdin) fclose(f);

    misure_ordina(m);
    printf("%s\n", titolo);
    for (size_t i = 0; i < misure_quante(m); i++) {
        testo_azzera();                         /* una riga, una stringa temporanea */
        const Misura *x = misure_prendi(m, i);
        const char *riga = testo_temp("%-12s %d", x->nome, x->valore);
        printf("  %s\n", riga != NULL ? riga : "(riga troppo lunga)");
    }

    if (misure_quante(m) >= 3) {
        const Misura *a = misure_prendi(m, 0), *b = misure_prendi(m, 1), *c = misure_prendi(m, 2);
        printf("media delle tre piu' piccole: %.1f\n",
               testo_media(3, a->valore, b->valore, c->valore));
    }
    printf("fine del %s (%zu misure, %zu aggiunte in tutto)\n",
           titolo, misure_quante(m), misure_totale);

    misure_free(m);
    return esito;
}
