/*
 * Esercizio 11.3 - L'ordinatore di righe
 *
 * Legge righe di lunghezza qualunque dall'ingresso e le stampa ordinate:
 *     ./bin/e03            alfabetico
 *     ./bin/e03 -r         al contrario
 *     ./bin/e03 -n         numerico (la chiave e' letta come numero)
 *     ./bin/e03 -k 2       la chiave e' la seconda parola della riga
 * Le opzioni si combinano: -k 2 -n -r.
 *
 * Il punto: il confronto dipende da tre cose decise a tempo di esecuzione
 * (quale campo, numerico o no, in che verso). Sono il CONTESTO del
 * comparatore. qsort non lo passa, quindi l'ordinamento e' scritto qui, con
 * il parametro ctx: un merge sort, che e' anche stabile (le righe con la
 * stessa chiave restano nell'ordine d'arrivo, cosa che qsort non garantisce).
 *
 * Scelte: le righe sono char * allocati da leggi_riga, in un array di
 * puntatori che cresce raddoppiando. Il merge sort lavora sull'array di
 * puntatori con un array d'appoggio della stessa misura. Tutto e' liberato
 * alla fine, anche nei percorsi d'errore (una sola etichetta).
 *
 * Errore tipico evitato: mettere le opzioni in variabili globali lette dal
 * comparatore di qsort. Funziona, finche' il programma ordina una cosa sola
 * per volta; il contesto esplicito funziona sempre, e si vede nella firma.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct { int campo; bool numerico, inverso; } Opzioni;

typedef int (*ConfrontoCtx)(const void *, const void *, void *ctx);

/* una riga senza l'a capo; e' del chiamante (free). NULL: fine file o memoria. */
static char *leggi_riga(FILE *f) {
    size_t n = 0, cap = 32;
    char *riga = malloc(cap);
    if (riga == NULL) return NULL;
    int c;
    while ((c = fgetc(f)) != EOF && c != '\n') {
        if (n + 1 == cap) {
            char *piu = realloc(riga, cap * 2);
            if (piu == NULL) { free(riga); return NULL; }
            riga = piu;
            cap *= 2;
        }
        riga[n++] = (char)c;
    }
    if (c == EOF && n == 0) { free(riga); return NULL; }
    riga[n] = '\0';
    return riga;
}

/* l'inizio della parola numero `campo` (da 1) della riga; la fine della riga se
   le parole sono meno. In prestito: punta dentro la riga. */
static const char *chiave(const char *riga, int campo) {
    const char *p = riga + strspn(riga, " \t");
    for (int i = 1; i < campo; i++) {
        p += strcspn(p, " \t");
        p += strspn(p, " \t");
    }
    return p;
}

/* a e b sono righe (char *); ctx sono le Opzioni */
static int confronta_righe(const void *a, const void *b, void *ctx) {
    const Opzioni *o = ctx;
    const char *x = chiave(a, o->campo), *y = chiave(b, o->campo);
    int r;
    if (o->numerico) {
        double nx = strtod(x, NULL), ny = strtod(y, NULL);
        r = (nx > ny) - (nx < ny);
    } else {
        r = strcmp(x, y);
    }
    return o->inverso ? -r : r;
}

/* merge sort stabile di un array di puntatori; appoggio ha la stessa lunghezza */
static void ordina(void **v, void **appoggio, size_t n, ConfrontoCtx cmp, void *ctx) {
    if (n < 2) return;
    size_t meta = n / 2;
    ordina(v, appoggio, meta, cmp, ctx);
    ordina(v + meta, appoggio, n - meta, cmp, ctx);

    size_t i = 0, j = meta, k = 0;
    while (i < meta && j < n)
        appoggio[k++] = cmp(v[j], v[i], ctx) < 0 ? v[j++] : v[i++];    /* a parita', il primo */
    while (i < meta) appoggio[k++] = v[i++];
    while (j < n) appoggio[k++] = v[j++];
    memcpy(v, appoggio, n * sizeof v[0]);
}

int main(int argc, char *argv[]) {
    Opzioni o = { .campo = 1, .numerico = false, .inverso = false };
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-r") == 0) o.inverso = true;
        else if (strcmp(argv[i], "-n") == 0) o.numerico = true;
        else if (strcmp(argv[i], "-k") == 0 && i + 1 < argc && atoi(argv[i + 1]) > 0)
            o.campo = atoi(argv[++i]);
        else {
            fprintf(stderr, "uso: %s [-r] [-n] [-k campo]\n", argv[0]);
            return 1;
        }
    }

    void **righe = NULL, **appoggio = NULL;
    size_t n = 0, cap = 0;
    int esito = 1;

    char *riga;
    while ((riga = leggi_riga(stdin)) != NULL) {
        if (n == cap) {
            size_t nuova = cap == 0 ? 16 : cap * 2;
            void **p = realloc(righe, nuova * sizeof *p);
            if (p == NULL) { free(riga); goto fine; }
            righe = p;
            cap = nuova;
        }
        righe[n++] = riga;
    }

    appoggio = malloc((n > 0 ? n : 1) * sizeof *appoggio);
    if (appoggio == NULL) goto fine;
    ordina(righe, appoggio, n, confronta_righe, &o);
    for (size_t i = 0; i < n; i++) puts(righe[i]);
    esito = 0;

fine:
    free(appoggio);
    for (size_t i = 0; i < n; i++) free(righe[i]);
    free(righe);
    return esito;
}
