/*
 * Esercizio 9.9 - ASSERT_MSG
 *
 * Idea: ASSERT_MSG(cond, msg) e' un do { if (!(cond)) { fprintf(...); abort(); } }
 * while (0): una sola istruzione, che sta anche sotto un if senza graffe. #cond
 * trasforma il testo della condizione in una stringa, incollata a quella del formato
 * ("ASSERT_MSG(" #cond ") fallita"); __FILE__ e __LINE__ sono del punto in cui la
 * macro e' usata. Con -DNDEBUG la macro diventa ((void)0): nessun controllo, nessun
 * codice, e per questo dentro ci vanno solo controlli, mai lavoro (niente i++, niente
 * chiamate con effetti).
 * La rubrica dinamica (Contatto, Rubrica con init/aggiungi/free e raddoppio) usa la
 * macro per i controlli interni: r->v != NULL (rubrica inizializzata), r->n <= r->cap
 * (invariante), e in rubrica_get l'indice dentro l'array.
 * Esecuzione normale: ./bin/e09 aggiunge quattro contatti e li stampa, termina con 0.
 * Per vedere un controllo che fallisce: ./bin/e09 fallisci chiede l'elemento n di una
 * rubrica con n contatti e la macro stampa su stderr qualcosa come
 *     e09.c:60: ASSERT_MSG(i < r->n) fallita: indice fuori dalla rubrica
 * e abort() ferma il programma (exit 134, SIGABRT); ./bin/e09 vuota usa una rubrica
 * mai inizializzata e fallisce il controllo su r->v. Compilato con -DNDEBUG
 * (gcc -std=c17 -DNDEBUG -o bin/e09 e09.c) i controlli spariscono e "fallisci" legge
 * oltre la fine dell'array: con ASan, heap-buffer-overflow.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef NDEBUG
#define ASSERT_MSG(cond, msg) ((void)0)
#else
#define ASSERT_MSG(cond, msg)                                                  \
    do {                                                                       \
        if (!(cond)) {                                                         \
            fprintf(stderr, "%s:%d: ASSERT_MSG(" #cond ") fallita: %s\n",      \
                    __FILE__, __LINE__, (msg));                                \
            abort();                                                           \
        }                                                                      \
    } while (0)
#endif

typedef struct {
    char nome[32];
    char telefono[16];
} Contatto;

typedef struct {
    Contatto *v;       /* il blocco sullo heap */
    size_t n;          /* quanti contatti ci sono */
    size_t cap;        /* quanti ce ne stanno */
} Rubrica;

Contatto contatto_new(const char *nome, const char *telefono) {
    Contatto c = {0};
    snprintf(c.nome, sizeof c.nome, "%s", nome);
    snprintf(c.telefono, sizeof c.telefono, "%s", telefono);
    return c;
}

bool rubrica_init(Rubrica *r, size_t cap) {
    ASSERT_MSG(cap > 0, "la capacita' iniziale deve essere positiva");
    r->v = malloc(cap * sizeof *r->v);
    if (r->v == NULL) {
        r->n = r->cap = 0;
        return false;
    }
    r->n = 0;
    r->cap = cap;
    return true;
}

bool rubrica_aggiungi(Rubrica *r, const Contatto *c) {
    ASSERT_MSG(r->v != NULL, "rubrica non inizializzata: manca rubrica_init");
    ASSERT_MSG(r->n <= r->cap, "invariante violato: piu' contatti della capacita'");
    if (r->n == r->cap) {                                  /* pieno: raddoppia */
        size_t nuova = r->cap * 2;
        Contatto *tmp = realloc(r->v, nuova * sizeof *tmp);
        if (tmp == NULL) return false;                     /* r->v e' ancora valido */
        r->v = tmp;
        r->cap = nuova;
    }
    size_t i = 0;
    while (i < r->n && strcmp(r->v[i].nome, c->nome) < 0) i++;
    memmove(&r->v[i + 1], &r->v[i], (r->n - i) * sizeof r->v[0]);
    r->v[i] = *c;
    r->n++;
    return true;
}

const Contatto *rubrica_get(const Rubrica *r, size_t i) {
    ASSERT_MSG(r->v != NULL, "rubrica non inizializzata: manca rubrica_init");
    ASSERT_MSG(i < r->n, "indice fuori dalla rubrica");
    return &r->v[i];
}

void rubrica_free(Rubrica *r) {
    free(r->v);
    r->v = NULL;
    r->n = r->cap = 0;
}

int main(int argc, char **argv) {
    const char *modo = argc > 1 ? argv[1] : "";

    if (strcmp(modo, "vuota") == 0) {
        Rubrica vuota = {0};                               /* mai inizializzata */
        Contatto c = contatto_new("Greta", "030 1");
        printf("aggiungo a una rubrica mai inizializzata: il controllo su r->v fallisce\n");
        rubrica_aggiungi(&vuota, &c);
        return 0;
    }

    Rubrica r;
    if (!rubrica_init(&r, 2)) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    ASSERT_MSG(r.v != NULL && r.cap == 2 && r.n == 0, "rubrica_init non ha allocato");

    const char *nomi[] = { "Nami", "Greta", "Max", "Loretta" };
    const char *telefoni[] = { "030 0", "030 1", "030 2", "030 3" };
    for (size_t i = 0; i < sizeof nomi / sizeof nomi[0]; i++) {
        Contatto c = contatto_new(nomi[i], telefoni[i]);
        if (!rubrica_aggiungi(&r, &c)) {
            fprintf(stderr, "memoria esaurita\n");
            rubrica_free(&r);
            return 1;
        }
    }
    ASSERT_MSG(r.n <= r.cap, "invariante violato dopo gli inserimenti");

    printf("Rubrica con %zu contatti (capacita' %zu), in ordine:\n", r.n, r.cap);
    for (size_t i = 0; i < r.n; i++) {
        const Contatto *c = rubrica_get(&r, i);
        printf("  [%zu] %-10s %s\n", i, c->nome, c->telefono);
    }

    if (strcmp(modo, "fallisci") == 0) {
        printf("chiedo l'elemento %zu di una rubrica con %zu contatti: il controllo "
               "sull'indice fallisce\n", r.n, r.n);
        const Contatto *c = rubrica_get(&r, r.n);          /* qui ASSERT_MSG ferma tutto */
        printf("(non si arriva qui senza -DNDEBUG) %s\n", c->nome);
    }

    rubrica_free(&r);
    printf("liberata: v = %p, n = %zu, cap = %zu\n", (void *)r.v, r.n, r.cap);
    return 0;
}
