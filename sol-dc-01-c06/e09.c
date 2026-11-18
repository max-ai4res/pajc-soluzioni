/*
 * Esercizio 6.9 - I quattro errori da trovare: il programma corretto
 *
 * La lista della spesa di Greta con le voci come stringhe allocate e un
 * "annulla" che rimette l'ultima voce tolta. Il programma con gli errori e'
 * e09_quattro.c (stessa struttura, stesse righe fino alla prima correzione);
 * questo e' il risultato della caccia con make asan-quattro e
 * ./bin/e09_quattro, un errore alla volta nell'ordine in cui ASan li ha
 * fermati. Le righe sono quelle di e09_quattro.c.
 *
 * 1. Overflow di un byte - duplica, righe 35 e 37.
 *    Sintomo: ERROR: AddressSanitizer: heap-buffer-overflow, WRITE of size 6
 *    in strcpy chiamata da duplica (e09_quattro.c:37) da lista_aggiungi;
 *    "0 bytes after 5-byte region" allocata in duplica (e09_quattro.c:35).
 *    Causa: malloc(strlen(s)) non conta il terminatore e strcpy scrive
 *    strlen + 1 byte: il classico byte di troppo delle stringhe. Senza ASan
 *    il byte finisce nel riempimento del blocco e nessuno se ne accorge.
 *    Correzione: malloc(strlen(s) + 1).
 *
 * 2. Uso dopo free - lista_sostituisci, righe 82 e 86.
 *    Sintomo: heap-use-after-free, READ of size 2 in printf chiamata da
 *    lista_sostituisci (e09_quattro.c:86); "freed by" lista_sostituisci
 *    (e09_quattro.c:82), blocco allocato in duplica da lista_aggiungi.
 *    Causa: la vecchia voce viene liberata e poi stampata. Senza ASan il
 *    blocco appena liberato e' lo stesso che malloc ridava per la copia
 *    nuova, e il programma stampava "al posto di pere ora c'e' pere": un
 *    indizio che sembra un refuso.
 *    Correzione: prima si stampa (con la voce ancora viva), poi si libera.
 *
 * 3. Doppio free - lista_annulla, riga 105 (la seconda free in lista_free,
 *    riga 117).
 *    Sintomo: attempting double-free in lista_free (e09_quattro.c:117),
 *    "freed by" lista_annulla (e09_quattro.c:105).
 *    Causa: lista_annulla libera ultima_tolta ma non la azzera, e lista_free
 *    la libera di nuovo. Senza ASan il programma non protestava: fra le due
 *    free il blocco era stato riassegnato alla stringa persa dal leak (il
 *    punto 4), e la seconda free liberava quella. Il malloc di sistema
 *    (macOS e glibc) intercetta un doppio free solo se il blocco e' ancora
 *    libero: e' per questo che ASan, che mette in quarantena i blocchi
 *    liberati, lo vede sempre.
 *    Correzione: l->ultima_tolta = NULL subito dopo la free (free + = NULL).
 *
 * 4. Leak - lista_contiene, riga 66 (le uscite alle righe 73 e 75).
 *    Sintomo: ASan su macOS tace; leaks --atExit -- ./bin/e09_quattro
 *    (eseguibile senza ASan, con le tre correzioni precedenti) riporta
 *    "2 leaks for 32 total leaked bytes", ROOT LEAK <malloc in duplica> da
 *    minuscola da lista_contiene (e09_quattro.c:66), una per chiamata. Sul
 *    programma con tutti e quattro gli errori ne riporta uno solo: l'altro
 *    blocco lo aveva liberato il doppio free del punto 3. Su Linux lo dice
 *    LeakSanitizer alla fine dell'esecuzione con ASan.
 *    Causa: la copia minuscola della voce cercata viene allocata e mai
 *    liberata, ne' nel return true ne' nel return false.
 *    Correzione: una sola uscita dal ciclo con la variabile trovata, e
 *    free(cercata) prima del return.
 *
 * Verifica: make asan && ./bin/e09 tace; make && leaks --atExit -- ./bin/e09
 * dice "0 leaks".
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
    char *copia = malloc(strlen(s) + 1);
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
    bool trovata = false;
    for (size_t i = 0; i < l->n && !trovata; i++) {
        char *m = minuscola(l->voci[i]);
        if (m == NULL) break;
        trovata = strcmp(m, cercata) == 0;
        free(m);
    }
    free(cercata);                        /* un solo punto d'uscita: niente leak */
    return trovata;
}

/* cambia la voce in posizione i; false se i e' fuori o la memoria manca */
bool lista_sostituisci(Lista *l, size_t i, const char *nuova) {
    if (i >= l->n) return false;
    char *copia = duplica(nuova);
    if (copia == NULL) return false;
    printf("al posto di %s ora c'e' %s\n", l->voci[i], nuova);
    free(l->voci[i]);
    l->voci[i] = copia;
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
    l->ultima_tolta = NULL;
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
