/*
 * Esercizio 7.8 - Un'arena che cresce
 *
 * Un'arena fatta di una catena di blocchi: quando quello corrente e' pieno se
 * ne alloca un altro. arena_alloc risponde NULL solo se il sistema non ha
 * piu' memoria. arena_free libera tutti i blocchi. Il programma legge un
 * testo, mette ogni parola nell'arena tenendo un array di puntatori, stampa
 * le parole in ordine alfabetico e libera tutto con una chiamata.
 *
 * Scelte: ogni blocco comincia con un'intestazione (il puntatore al blocco
 * precedente, quanti byte ha, quanti ne ha usati) seguita dai dati: una
 * malloc sola per intestazione e dati, con l'intestazione arrotondata
 * all'allineamento massimo cosi' i dati partono allineati. I blocchi vecchi
 * non si riusano: resta in fondo qualche byte sprecato, ed e' il prezzo.
 * L'array dei puntatori alle parole NON sta nell'arena: deve crescere con
 * realloc, e un pezzo dell'arena non si puo' allargare.
 *
 * Errore tipico evitato: allocare sempre un blocco di misura fissa. Se una
 * richiesta e' piu' grande del blocco (una parola di 10000 lettere con
 * blocchi da 4096) il blocco nuovo non basterebbe. La misura del blocco nuovo
 * e' il massimo fra quella abituale e la richiesta.
 */
#include <stdalign.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define BLOCCO 4096

typedef struct Blocco Blocco;
struct Blocco {
    Blocco *precedente;
    size_t cap, usati;       /* dei soli dati, che seguono l'intestazione */
};

typedef struct { Blocco *corrente; size_t blocchi; } Arena;

static size_t arrotonda(size_t n) {
    size_t passo = alignof(max_align_t);
    return (n + passo - 1) / passo * passo;
}

static char *dati(Blocco *b) { return (char *)b + arrotonda(sizeof(Blocco)); }

void *arena_alloc(Arena *a, size_t n) {
    n = arrotonda(n);
    Blocco *b = a->corrente;
    if (b == NULL || n > b->cap - b->usati) {
        size_t cap = n > BLOCCO ? n : BLOCCO;           /* almeno quanto serve */
        b = malloc(arrotonda(sizeof(Blocco)) + cap);
        if (b == NULL) return NULL;
        *b = (Blocco){ .precedente = a->corrente, .cap = cap, .usati = 0 };
        a->corrente = b;
        a->blocchi++;
    }
    void *p = dati(b) + b->usati;
    b->usati += n;
    return p;
}

char *arena_str(Arena *a, const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = arena_alloc(a, n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

/* libera tutti i blocchi: ogni puntatore preso dall'arena smette di valere */
void arena_free(Arena *a) {
    Blocco *b = a->corrente;
    while (b != NULL) {
        Blocco *precedente = b->precedente;     /* letto prima di liberare b */
        free(b);
        b = precedente;
    }
    *a = (Arena){ 0 };
}

static int cmp_str(const void *x, const void *y) {
    return strcmp(*(char *const *)x, *(char *const *)y);
}

int main(void) {
    Arena arena = { 0 };
    char **parole = NULL;                       /* in prestito dall'arena */
    size_t n = 0, cap = 0;
    int esito = 0;

    char buf[256];
    while (scanf("%255s", buf) == 1) {
        if (n == cap) {
            size_t nuova = cap == 0 ? 64 : cap * 2;
            char **p = realloc(parole, nuova * sizeof *p);
            if (p == NULL) { esito = 1; break; }
            parole = p;
            cap = nuova;
        }
        char *copia = arena_str(&arena, buf);
        if (copia == NULL) { esito = 1; break; }
        parole[n++] = copia;
    }

    if (esito == 0) {
        qsort(parole, n, sizeof parole[0], cmp_str);
        for (size_t i = 0; i < n; i++) printf("%s\n", parole[i]);
        fprintf(stderr, "%zu parole in %zu blocchi\n", n, arena.blocchi);

        void *grande = arena_alloc(&arena, 10000);          /* piu' di un blocco */
        fprintf(stderr, "10000 byte: %s, ora %zu blocchi\n",
                grande != NULL ? "presi" : "no", arena.blocchi);
    }

    free(parole);                               /* l'array: malloc/realloc */
    arena_free(&arena);                         /* tutte le parole, in un colpo */
    return esito;
}
