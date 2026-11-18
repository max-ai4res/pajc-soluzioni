/*
 * Esercizio 7.6 - Una riga lunga quanto serve
 *
 * leggi_riga(f) legge una riga di lunghezza qualunque e la restituisce senza
 * l'a capo, in un blocco che il chiamante libera con free; NULL alla fine del
 * file (o se manca la memoria). Il main numera le righe lette da tastiera e
 * le ristampa con str_printf, che alloca una stringa lunga quanto serve.
 *
 * Scelte: leggi_riga legge un carattere alla volta con fgetc in un buffer che
 * raddoppia. fgetc restituisce un int, perche' EOF non e' un carattere: la
 * variabile e' int, non char. Il contratto e' scritto sopra ogni funzione.
 *
 * Errore tipico evitato: confondere "fine del file" con "riga vuota". NULL
 * vuol dire che non c'e' piu' niente da leggere; una riga vuota e' una
 * stringa di lunghezza zero, allocata. E l'ultima riga di un file puo' non
 * finire con '\n': se sono stati letti dei caratteri prima di EOF, e' una
 * riga, e va restituita. NULL esce solo se EOF arriva senza aver letto nulla.
 */
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* una riga senza l'a capo. E' del chiamante (free). NULL: fine file o memoria. */
char *leggi_riga(FILE *f) {
    size_t n = 0, cap = 16;
    char *riga = malloc(cap);
    if (riga == NULL) return NULL;

    int c;
    while ((c = fgetc(f)) != EOF && c != '\n') {
        if (n + 1 == cap) {                     /* resta posto per il terminatore? */
            char *piu = realloc(riga, cap * 2);
            if (piu == NULL) { free(riga); return NULL; }
            riga = piu;
            cap *= 2;
        }
        riga[n++] = (char)c;
    }
    if (c == EOF && n == 0) {                   /* niente da leggere */
        free(riga);
        return NULL;
    }
    riga[n] = '\0';
    return riga;
}

/* come sprintf, ma alloca. Il risultato e' del chiamante (free). */
__attribute__((format(printf, 1, 2)))
char *str_printf(const char *fmt, ...) {
    va_list ap, ap2;
    va_start(ap, fmt);
    va_copy(ap2, ap);
    int n = vsnprintf(NULL, 0, fmt, ap2);
    va_end(ap2);
    char *s = n < 0 ? NULL : malloc((size_t)n + 1);
    if (s != NULL) vsnprintf(s, (size_t)n + 1, fmt, ap);
    va_end(ap);
    return s;
}

int main(void) {
    char *riga;
    for (int numero = 1; (riga = leggi_riga(stdin)) != NULL; numero++) {
        char *testo = str_printf("%3d (%zu): \"%s\"", numero, strlen(riga), riga);
        if (testo != NULL) puts(testo);
        free(testo);
        free(riga);
    }
    return 0;
}
