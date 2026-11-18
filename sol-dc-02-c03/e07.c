/*
 * Esercizio 3.7 - La classifica
 *
 * Legge coppie nome e punteggio (al piu' MAX) in un array di Giocatore; stampa
 * la classifica per punteggio decrescente e, a parita', per nome; poi riordina
 * per nome e cerca con bsearch i nomi ricevuti dalla riga di comando.
 *
 * Scelte: due comparatori per qsort (per_punti, per_nome) e uno per bsearch
 * (chiave_nome), che riceve come primo argomento la chiave, una stringa, e
 * come secondo un Giocatore: non serve costruire un giocatore finto. per_nome
 * e chiave_nome usano lo stesso strcmp sul nome: e' cio' che rende lecita la
 * ricerca binaria.
 *
 * Errore tipico evitato: chiamare bsearch con chiave_nome sull'array ancora
 * ordinato per punti. Non da' errori: risponde "non c'e'" per una parte dei
 * giocatori. Il riordino per nome viene prima della ricerca, sempre.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX 100

typedef struct { char nome[32]; int punti; } Giocatore;

static int per_punti(const void *a, const void *b) {
    const Giocatore *x = a, *y = b;
    if (x->punti != y->punti) return (x->punti < y->punti) - (x->punti > y->punti);
    return strcmp(x->nome, y->nome);
}

static int per_nome(const void *a, const void *b) {
    const Giocatore *x = a, *y = b;
    return strcmp(x->nome, y->nome);
}

static int chiave_nome(const void *chiave, const void *elemento) {
    const Giocatore *g = elemento;
    return strcmp(chiave, g->nome);
}

int main(int argc, char *argv[]) {
    Giocatore g[MAX];
    size_t n = 0;
    while (n < MAX && scanf("%31s %d", g[n].nome, &g[n].punti) == 2) n++;

    qsort(g, n, sizeof g[0], per_punti);
    printf("Classifica:\n");
    for (size_t i = 0; i < n; i++) printf("%2zu. %-12s %d\n", i + 1, g[i].nome, g[i].punti);

    qsort(g, n, sizeof g[0], per_nome);          /* prima di cercare per nome */
    for (int i = 1; i < argc; i++) {
        const Giocatore *t = bsearch(argv[i], g, n, sizeof g[0], chiave_nome);
        if (t == NULL) printf("%s: non c'e'\n", argv[i]);
        else printf("%s: %d punti\n", t->nome, t->punti);
    }
    return 0;
}
