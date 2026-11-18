/*
 * Esercizio 2.6 - Saluta tutti
 *
 * Saluta ogni argomento della riga di comando, uno per riga, e alla fine
 * stampa quanti nomi ha salutato e quante lettere hanno in tutto (strlen).
 * Senza argomenti stampa l'uso su stderr e termina con codice 1.
 *
 * Errore tipico evitato: argc conta anche argv[0], il nome del programma.
 * Il ciclo parte da 1 e "senza argomenti" vuol dire argc == 1, non 0.
 * Il totale delle lettere e' un size_t perche' strlen restituisce size_t;
 * si stampa con %zu.
 *
 * Prova: ./bin/e06 Greta Max Nami
 */
#include <stdio.h>
#include <string.h>

int main(int argc, char *argv[]) {
    if (argc == 1) {
        fprintf(stderr, "uso: %s nome [nome ...]\n", argv[0]);
        return 1;
    }

    size_t lettere = 0;
    for (int i = 1; i < argc; i++) {
        printf("Ciao, %s!\n", argv[i]);
        lettere += strlen(argv[i]);
    }

    printf("Salutati %d nomi, %zu lettere in tutto.\n", argc - 1, lettere);
    return 0;
}
