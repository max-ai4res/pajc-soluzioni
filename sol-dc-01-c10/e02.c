/* Esercizio 10.2 - La stringa dinamica
 * leggi_riga legge con fgetc, un carattere alla volta, in un buffer sullo heap che
 * parte da 16 byte e raddoppia con realloc (variabile d'appoggio) quando e' pieno.
 * Il controllo e' "n + 1 == cap" PRIMA di scrivere il carattere: cosi' resta sempre un
 * byte libero per lo zero finale, e una riga lunga esattamente cap caratteri non scrive
 * lo zero fuori dal blocco (l'errore tipico, che ASan vede e l'occhio no). La variabile
 * c e' int perche' deve poter contenere EOF.
 * concatena e duplica allocano strlen + 1 e copiano con memcpy: conti esatti, niente
 * strcpy in buffer non dimensionati. Ogni funzione che restituisce un blocco dice nel
 * commento chi lo libera (il chiamante, con free).
 * Il main legge righe da stdin fino a EOF (anche da pipe), le unisce con uno spazio in
 * una stringa sola, stampa stringa e lunghezza, e libera ogni blocco intermedio.
 * Prova: printf 'uno\ndue\ntre\n' | ./bin/e02
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum { CAP_INIZIALE = 16 };

/* Legge una riga di lunghezza qualunque da f, senza l'a capo. Restituisce un blocco
 * nuovo che il chiamante libera con free; NULL alla fine del file (o se la memoria e'
 * esaurita: i due casi si distinguono con feof(f), se serve). */
char *leggi_riga(FILE *f) {
    size_t cap = CAP_INIZIALE;
    size_t n = 0;
    char *buf = malloc(cap);
    if (buf == NULL) return NULL;
    int c;
    while ((c = fgetc(f)) != EOF && c != '\n') {
        if (n + 1 == cap) {                   /* pieno, contando lo zero finale */
            char *tmp = realloc(buf, cap * 2);
            if (tmp == NULL) {
                free(buf);                    /* buf e' ancora valido: va liberato */
                return NULL;
            }
            buf = tmp;
            cap *= 2;
        }
        buf[n++] = (char)c;
    }
    if (c == EOF && n == 0) {                 /* fine del file, nessun carattere */
        free(buf);
        return NULL;
    }
    buf[n] = '\0';                            /* c'e' posto: n < cap */
    return buf;
}

/* a seguita da b in un blocco nuovo; il chiamante libera con free. NULL se la memoria
 * e' esaurita. */
char *concatena(const char *a, const char *b) {
    size_t la = strlen(a), lb = strlen(b);
    char *r = malloc(la + lb + 1);
    if (r == NULL) return NULL;
    memcpy(r, a, la);
    memcpy(r + la, b, lb + 1);                /* lb + 1: anche lo zero di b */
    return r;
}

/* Copia di s in un blocco nuovo; il chiamante libera con free. */
char *duplica(const char *s) {
    size_t n = strlen(s) + 1;
    char *r = malloc(n);
    if (r == NULL) return NULL;
    memcpy(r, s, n);
    return r;
}

int main(void) {
    char *tutto = NULL;                       /* la stringa che cresce */
    size_t righe = 0;
    char *riga;
    while ((riga = leggi_riga(stdin)) != NULL) {
        righe++;
        printf("riga %zu: \"%s\" (%zu caratteri)\n", righe, riga, strlen(riga));
        char *nuovo;
        if (tutto == NULL) {
            nuovo = duplica(riga);
        } else {
            char *con_spazio = concatena(tutto, " ");
            if (con_spazio == NULL) {
                free(riga);
                free(tutto);
                fprintf(stderr, "memoria esaurita\n");
                return 1;
            }
            nuovo = concatena(con_spazio, riga);
            free(con_spazio);
            free(tutto);                      /* la versione precedente non serve piu' */
        }
        free(riga);
        if (nuovo == NULL) {
            fprintf(stderr, "memoria esaurita\n");
            return 1;
        }
        tutto = nuovo;
    }
    if (tutto == NULL) {
        printf("nessuna riga letta\n");
        return 0;
    }
    printf("%zu righe unite: \"%s\" (%zu caratteri)\n", righe, tutto, strlen(tutto));
    free(tutto);
    tutto = NULL;
    return 0;
}
