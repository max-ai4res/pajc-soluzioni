/*
 * Esercizio 9.8 - Il Vettore come modulo
 *
 * Il Vettore di void * (con vettore_rimuovi) diventa un modulo: e08_vettore.h
 * dichiara il tipo opaco e otto funzioni, e08_vettore.c contiene la struct e
 * i corpi. Questo file e' un cliente: legge parole, le mette nel vettore (che
 * le possiede), le ordina, toglie quelle piu' corte di quattro lettere e
 * stampa le rimaste. E' lo stesso programma del problema 4.8, in cui pero'
 * `parole->n = 0` qui non compila piu': il main non conosce i campi.
 *
 * Verifica: `make nomi-e08` compila il solo modulo e mostra con nm i suoi
 * nomi. Quelli con la T maiuscola (visibili al linker) sono gli otto
 * dell'header; `cresci`, static, ha la t minuscola.
 *
 * Scelte: nell'header ci sono anche i tre typedef dei puntatori a funzione
 * (Libera, Visita, Confronto), perche' fanno parte delle firme pubbliche. Il
 * contratto di possesso sta nei commenti dell'header, che e' cio' che un
 * cliente legge.
 *
 * Errore tipico evitato: un sorgente di modulo che non include il proprio
 * header. Se prototipo e definizione non coincidono, i due file compilano
 * ciascuno per conto suo e il linker li unisce lo stesso. e08_vettore.c
 * include e08_vettore.h per primo: la discordanza diventa un errore di
 * compilazione.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "e08_vettore.h"

static char *str_dup(const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = malloc(n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

static int cmp_str(const void *a, const void *b) {
    return strcmp(*(char *const *)a, *(char *const *)b);
}

static void stampa(void *elemento, void *ctx) {
    (void)ctx;
    printf("%s ", (const char *)elemento);
}

int main(void) {
    Vettore *parole = vettore_new(free);
    if (parole == NULL) return 1;

    char buf[64];
    while (scanf("%63s", buf) == 1) {
        char *copia = str_dup(buf);
        if (copia == NULL || !vettore_aggiungi(parole, copia)) {
            free(copia);
            break;
        }
    }

    vettore_ordina(parole, cmp_str);
    for (size_t i = 0; i < vettore_quanti(parole); ) {
        const char *p = vettore_prendi(parole, i);
        if (strlen(p) < 4) vettore_rimuovi(parole, i);
        else i++;
    }

    vettore_per_ognuno(parole, stampa, NULL);
    printf("\n");
    vettore_free(parole);
    return 0;
}
