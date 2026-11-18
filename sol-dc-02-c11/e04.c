/*
 * Esercizio 11.4 - Il registro eventi
 *
 * Un piccolo sistema di eventi: evento_registra(livello, fmt, ...) compone un
 * messaggio e lo consegna a tutti gli ascoltatori iscritti il cui livello
 * minimo e' raggiunto. Un ascoltatore e' una callback con il suo contesto:
 * qui ce ne sono tre, uno che scrive su stderr, uno che conta gli eventi per
 * livello, uno che conserva gli ultimi messaggi gravi in un vettore.
 *
 * Mette insieme: la funzione variadica con l'inoltro a vsnprintf in due passi
 * (cap. 5 e 7), l'annotazione del formato, la callback con il void *ctx (cap.
 * 3), la tabella di struct con un puntatore a funzione (cap. 2), il contratto
 * di possesso (cap. 7).
 *
 * Scelte: il messaggio e' composto UNA volta e prestato a ogni ascoltatore:
 * vale solo per la durata della chiamata. L'ascoltatore che vuole
 * conservarlo se ne fa una copia (conserva lo fa, e libera le sue copie in
 * archivio_libera). Gli ascoltatori stanno in un array statico a dimensione
 * fissa: evento_ascolta restituisce false se e' pieno.
 *
 * Errore tipico evitato: conservare il puntatore ricevuto dalla callback. Il
 * messaggio viene liberato appena il giro degli ascoltatori finisce:
 * l'archivio conterrebbe puntatori pendenti (ASan: heap-use-after-free alla
 * stampa finale).
 */
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef enum { DEBUG, INFO, AVVISO, ERRORE, N_LIVELLI } Livello;

static const char *const NOMI[N_LIVELLI] = { "DEBUG", "INFO", "AVVISO", "ERRORE" };

/* il messaggio e' in prestito: vale fino al ritorno della callback */
typedef void (*Ascoltatore)(Livello livello, const char *messaggio, void *ctx);

typedef struct { Ascoltatore f; void *ctx; Livello minimo; } Iscrizione;

#define MAX_ASCOLTATORI 8
static Iscrizione iscritti[MAX_ASCOLTATORI];
static size_t n_iscritti = 0;

/* iscrive f, che ricevera' gli eventi dal livello minimo in su. ctx e' in
   prestito: deve vivere finche' arrivano eventi. false se non c'e' posto. */
bool evento_ascolta(Ascoltatore f, void *ctx, Livello minimo) {
    if (n_iscritti == MAX_ASCOLTATORI) return false;
    iscritti[n_iscritti++] = (Iscrizione){ f, ctx, minimo };
    return true;
}

__attribute__((format(printf, 2, 3)))
void evento_registra(Livello livello, const char *fmt, ...) {
    va_list ap, ap2;
    va_start(ap, fmt);
    va_copy(ap2, ap);
    int n = vsnprintf(NULL, 0, fmt, ap2);
    va_end(ap2);
    char *messaggio = n < 0 ? NULL : malloc((size_t)n + 1);
    if (messaggio != NULL) vsnprintf(messaggio, (size_t)n + 1, fmt, ap);
    va_end(ap);
    if (messaggio == NULL) return;

    for (size_t i = 0; i < n_iscritti; i++)
        if (livello >= iscritti[i].minimo) iscritti[i].f(livello, messaggio, iscritti[i].ctx);
    free(messaggio);                            /* il prestito finisce qui */
}

/* --- tre ascoltatori --- */
static void su_stderr(Livello livello, const char *messaggio, void *ctx) {
    const char *prefisso = ctx;
    fprintf(stderr, "%s%-6s %s\n", prefisso, NOMI[livello], messaggio);
}

static void conta(Livello livello, const char *messaggio, void *ctx) {
    (void)messaggio;
    int *conti = ctx;                           /* un array di N_LIVELLI int */
    conti[livello]++;
}

typedef struct { char *v[4]; size_t n; } Archivio;      /* gli ultimi 4, in copia */

static void conserva(Livello livello, const char *messaggio, void *ctx) {
    (void)livello;
    Archivio *a = ctx;
    size_t lung = strlen(messaggio) + 1;
    char *copia = malloc(lung);                 /* una copia: il messaggio non e' nostro */
    if (copia == NULL) return;
    memcpy(copia, messaggio, lung);
    if (a->n == 4) {                            /* pieno: esce il piu' vecchio */
        free(a->v[0]);
        memmove(&a->v[0], &a->v[1], 3 * sizeof a->v[0]);
        a->n--;
    }
    a->v[a->n++] = copia;
}

static void archivio_libera(Archivio *a) {
    for (size_t i = 0; i < a->n; i++) free(a->v[i]);
    a->n = 0;
}

int main(void) {
    int conti[N_LIVELLI] = { 0 };
    Archivio gravi = { .n = 0 };
    char prefisso[] = "[chat] ";

    evento_ascolta(su_stderr, prefisso, INFO);
    evento_ascolta(conta, conti, DEBUG);
    evento_ascolta(conserva, &gravi, AVVISO);

    evento_registra(DEBUG, "avvio, %d ascoltatori", (int)n_iscritti);
    evento_registra(INFO, "%s e' entrata", "Greta");
    evento_registra(AVVISO, "nome troppo lungo: %s", "Massimiliano_il_Grande");
    for (int i = 1; i <= 5; i++) evento_registra(ERRORE, "errore numero %d", i);
    evento_registra(INFO, "%s e' uscita", "Greta");

    for (int l = 0; l < N_LIVELLI; l++) printf("%-6s %d\n", NOMI[l], conti[l]);
    printf("ultimi messaggi gravi:\n");
    for (size_t i = 0; i < gravi.n; i++) printf("  %s\n", gravi.v[i]);
    archivio_libera(&gravi);
    return 0;
}
