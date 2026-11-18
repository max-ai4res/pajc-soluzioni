/*
 * Esercizio 3.6 - Le parole in ordine
 *
 * Ordina le parole ricevute dalla riga di comando: in ordine alfabetico, oppure
 * con -l per lunghezza crescente e, a parita', alfabetico. qsort ordina
 * direttamente argv, cioe' sposta i puntatori alle parole, non le lettere.
 *
 * Scelte: i due criteri sono due comparatori con la stessa firma, e l'opzione
 * sceglie quale passare; la chiamata a qsort e' una sola. L'array da ordinare
 * comincia da argv[1] (o argv[2] con l'opzione): argv[0] e' il nome del
 * programma e non va toccato.
 *
 * Errore tipico evitato: gli elementi di argv sono char *, quindi al
 * comparatore arrivano due char ** (indirizzi di elementi). Passare strcmp
 * con un cast compila e confronta i byte dei puntatori invece delle lettere.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int alfabetico(const void *a, const void *b) {
    const char *x = *(char *const *)a, *y = *(char *const *)b;
    return strcmp(x, y);
}

static int per_lunghezza(const void *a, const void *b) {
    const char *x = *(char *const *)a, *y = *(char *const *)b;
    size_t lx = strlen(x), ly = strlen(y);
    if (lx != ly) return (lx > ly) - (lx < ly);
    return strcmp(x, y);
}

int main(int argc, char *argv[]) {
    int (*criterio)(const void *, const void *) = alfabetico;
    int primo = 1;
    if (argc > 1 && strcmp(argv[1], "-l") == 0) {
        criterio = per_lunghezza;
        primo = 2;
    }

    size_t n = (size_t)(argc - primo);
    qsort(argv + primo, n, sizeof argv[0], criterio);
    for (size_t i = 0; i < n; i++) printf("%s\n", argv[primo + i]);
    return 0;
}
