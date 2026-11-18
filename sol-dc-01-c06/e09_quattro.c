/*
 * Esercizio 6.9 - I quattro errori da trovare: il programma CON gli errori
 *
 * La lista della spesa di Greta, con le voci come stringhe allocate una per
 * una e un "annulla" che rimette l'ultima voce tolta. Compila pulito con
 * -Wall -Wextra -Werror, gira senza lamentarsi, e contiene un leak, un uso
 * dopo free, un doppio free e un overflow di un byte, uno per funzione.
 * Si compila con make asan-quattro (o make asan) e si lancia
 * ./bin/e09_quattro: ASan si ferma al primo errore; si corregge, si
 * ricompila, si ripete. Il leak su macOS non lo vede ASan ma leaks:
 *   make && leaks --atExit -- ./bin/e09_quattro
 * Il programma corretto e il diario della caccia sono in e09.c.
 */
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char **voci;          /* n stringhe allocate, in un blocco da cap puntatori */
    size_t n, cap;
    char *ultima_tolta;   /* la stringa dell'ultima voce tolta, per annulla, o NULL */
} Lista;

void lista_init(Lista *l) {
    l->voci = NULL;
    l->n = 0;
    l->cap = 0;
    l->ultima_tolta = NULL;
}

/* copia di s sullo heap; il chiamante libera */
static char *duplica(const char *s) {
    char *copia = malloc(strlen(s));
    if (copia == NULL) return NULL;
    strcpy(copia, s);
    return copia;
}

/* copia di s tutta minuscola, sullo heap; il chiamante libera */
static char *minuscola(const char *s) {
    char *m = duplica(s);
    if (m == NULL) return NULL;
    for (size_t i = 0; m[i] != '\0'; i++) m[i] = (char)tolower((unsigned char)m[i]);
    return m;
}

/* aggiunge una copia della voce in coda; false se la memoria manca */
bool lista_aggiungi(Lista *l, const char *voce) {
    if (l->n == l->cap) {
        size_t nuova = l->cap == 0 ? 4 : 2 * l->cap;
        char **tmp = realloc(l->voci, nuova * sizeof *tmp);
        if (tmp == NULL) return false;
        l->voci = tmp;
        l->cap = nuova;
    }
    char *copia = duplica(voce);
    if (copia == NULL) return false;
    l->voci[l->n++] = copia;
    return true;
}

/* vero se la voce c'e', senza badare a maiuscole e minuscole */
bool lista_contiene(const Lista *l, const char *voce) {
    char *cercata = minuscola(voce);
    if (cercata == NULL) return false;
    for (size_t i = 0; i < l->n; i++) {
        char *m = minuscola(l->voci[i]);
        if (m == NULL) return false;
        bool uguale = strcmp(m, cercata) == 0;
        free(m);
        if (uguale) return true;
    }
    return false;
}

/* cambia la voce in posizione i; false se i e' fuori o la memoria manca */
bool lista_sostituisci(Lista *l, size_t i, const char *nuova) {
    if (i >= l->n) return false;
    char *vecchia = l->voci[i];
    free(vecchia);
    char *copia = duplica(nuova);
    if (copia == NULL) return false;
    l->voci[i] = copia;
    printf("al posto di %s ora c'e' %s\n", vecchia, copia);
    return true;
}

/* toglie la voce in posizione i chiudendo il buco; la tiene per annulla */
void lista_rimuovi(Lista *l, size_t i) {
    if (i >= l->n) return;
    free(l->ultima_tolta);                /* la tolta precedente non serve piu' */
    l->ultima_tolta = l->voci[i];
    memmove(&l->voci[i], &l->voci[i + 1], (l->n - i - 1) * sizeof l->voci[0]);
    l->n--;
    printf("tolta: %s\n", l->ultima_tolta);
}

/* rimette in coda l'ultima voce tolta; false se non c'e' nulla da annullare */
bool lista_annulla(Lista *l) {
    if (l->ultima_tolta == NULL) return false;
    if (!lista_aggiungi(l, l->ultima_tolta)) return false;
    printf("rimessa: %s\n", l->ultima_tolta);
    free(l->ultima_tolta);
    return true;
}

void lista_stampa(const Lista *l) {
    printf("lista della spesa (%zu voci):\n", l->n);
    for (size_t i = 0; i < l->n; i++) printf("  %zu. %s\n", i + 1, l->voci[i]);
}

void lista_free(Lista *l) {
    for (size_t i = 0; i < l->n; i++) free(l->voci[i]);
    free(l->voci);
    free(l->ultima_tolta);
    lista_init(l);
}

int main(void) {
    Lista spesa;
    lista_init(&spesa);
    const char *voci[] = { "latte", "pane", "uova", "caffe'", "mele" };
    for (size_t i = 0; i < sizeof voci / sizeof voci[0]; i++) {
        if (!lista_aggiungi(&spesa, voci[i])) {
            fprintf(stderr, "memoria insufficiente\n");
            lista_free(&spesa);
            return EXIT_FAILURE;
        }
    }
    lista_stampa(&spesa);

    lista_sostituisci(&spesa, 4, "pere");
    lista_rimuovi(&spesa, 1);
    lista_stampa(&spesa);
    printf("annullo? %s\n", lista_annulla(&spesa) ? "si'" : "no");

    printf("c'e' il Pane? %s\n", lista_contiene(&spesa, "Pane") ? "si'" : "no");
    printf("c'e' il burro? %s\n", lista_contiene(&spesa, "burro") ? "si'" : "no");
    lista_stampa(&spesa);

    lista_free(&spesa);
    return 0;
}
