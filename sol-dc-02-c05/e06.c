/*
 * Esercizio 5.6 - Conteggio e sentinella
 *
 * Due funzioni variadiche con le due convenzioni per dire "quanti":
 * massimo(n, ...) riceve il conteggio come primo argomento; concatena(prima,
 * ...) riceve stringhe fino a una sentinella NULL e restituisce la loro
 * concatenazione in un blocco nuovo, che il chiamante libera con free.
 *
 * Scelte: massimo richiede n >= 1 (il massimo di zero numeri non esiste) e lo
 * dice con un assert. concatena percorre gli argomenti due volte: la prima
 * per sommare le lunghezze, la seconda per copiare. Sono due cursori, ognuno
 * con il suo va_start e il suo va_end.
 *
 * Errore tipico evitato: riusare la stessa va_list per il secondo giro. Dopo
 * il primo ciclo il cursore e' in fondo, e non c'e' modo di "riavvolgerlo":
 * si chiude e se ne apre un altro (o si fa va_copy prima di consumarlo).
 * E la sentinella si passa come (const char *)NULL: un NULL senza cast, fra i
 * puntini, potrebbe essere passato come int.
 */
#include <assert.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int massimo(int n, ...) {
    assert(n >= 1);
    va_list ap;
    va_start(ap, n);
    int max = va_arg(ap, int);
    for (int i = 1; i < n; i++) {
        int x = va_arg(ap, int);
        if (x > max) max = x;
    }
    va_end(ap);
    return max;
}

/* il risultato e' del chiamante: va liberato con free; NULL se manca memoria */
char *concatena(const char *prima, ...) {
    va_list ap;
    size_t lunghezza = 0;

    va_start(ap, prima);                               /* primo giro: quanto serve */
    for (const char *s = prima; s != NULL; s = va_arg(ap, const char *))
        lunghezza += strlen(s);
    va_end(ap);

    char *risultato = malloc(lunghezza + 1);
    if (risultato == NULL) return NULL;

    char *p = risultato;
    va_start(ap, prima);                               /* secondo giro: copia */
    for (const char *s = prima; s != NULL; s = va_arg(ap, const char *)) {
        size_t n = strlen(s);
        memcpy(p, s, n);
        p += n;
    }
    va_end(ap);
    *p = '\0';
    return risultato;
}

int main(void) {
    printf("%d\n", massimo(4, 3, -7, 42, 19));
    printf("%d\n", massimo(1, -5));

    char *s = concatena("ciao", ", ", "Greta", "!", (const char *)NULL);
    char *t = concatena("sola", (const char *)NULL);
    if (s == NULL || t == NULL) { free(s); free(t); return 1; }
    printf("%s\n%s\n", s, t);
    free(s);
    free(t);
    return 0;
}
