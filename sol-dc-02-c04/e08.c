/*
 * Esercizio 4.8 - Il Vettore completo
 *
 * Il Vettore di void * del capitolo, con vettore_rimuovi e vettore_ordina. Il
 * programma legge parole da tastiera, ne mette una copia nel vettore (che le
 * possiede: creato con vettore_new(free)), le ordina, rimuove quelle piu'
 * corte di quattro lettere e stampa le rimaste.
 *
 * Scelte: vettore_rimuovi libera l'elemento se il vettore lo possiede, poi fa
 * scorrere di un posto i puntatori successivi con memmove (i due blocchi si
 * sovrappongono: memcpy non va). Chi non vuole che l'elemento venga liberato
 * non deve usare un vettore che possiede.
 *
 * Errore tipico evitato: nel ciclo di rimozione, dopo aver tolto l'elemento i
 * il successivo ha preso il suo posto, quindi l'indice NON avanza. Un for con
 * i++ incondizionato salterebbe un elemento dopo ogni rimozione.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef void (*Libera)(void *elemento);
typedef void (*Visita)(void *elemento, void *ctx);

typedef struct {
    void **v;
    size_t n, cap;
    Libera libera;
} Vettore;

Vettore *vettore_new(Libera libera) {
    Vettore *vt = malloc(sizeof *vt);
    if (vt == NULL) return NULL;
    *vt = (Vettore){ .v = NULL, .n = 0, .cap = 0, .libera = libera };
    return vt;
}

bool vettore_aggiungi(Vettore *vt, void *elemento) {
    if (vt->n == vt->cap) {
        size_t cap = vt->cap == 0 ? 4 : vt->cap * 2;
        void **v = realloc(vt->v, cap * sizeof *v);
        if (v == NULL) return false;
        vt->v = v;
        vt->cap = cap;
    }
    vt->v[vt->n++] = elemento;
    return true;
}

size_t vettore_quanti(const Vettore *vt) { return vt->n; }

void *vettore_prendi(const Vettore *vt, size_t i) {
    return i < vt->n ? vt->v[i] : NULL;
}

void vettore_per_ognuno(const Vettore *vt, Visita f, void *ctx) {
    for (size_t i = 0; i < vt->n; i++) f(vt->v[i], ctx);
}

/* toglie l'elemento i (liberandolo, se il vettore lo possiede) */
bool vettore_rimuovi(Vettore *vt, size_t i) {
    if (i >= vt->n) return false;
    if (vt->libera != NULL) vt->libera(vt->v[i]);
    memmove(&vt->v[i], &vt->v[i + 1], (vt->n - i - 1) * sizeof vt->v[0]);
    vt->n--;
    return true;
}

void vettore_ordina(Vettore *vt, int (*cmp)(const void *, const void *)) {
    qsort(vt->v, vt->n, sizeof vt->v[0], cmp);
}

void vettore_free(Vettore *vt) {
    if (vt == NULL) return;
    if (vt->libera != NULL)
        for (size_t i = 0; i < vt->n; i++) vt->libera(vt->v[i]);
    free(vt->v);
    free(vt);
}

static char *str_dup(const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = malloc(n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

/* gli elementi sono char *: arrivano due char ** */
static int cmp_str(const void *a, const void *b) {
    return strcmp(*(char *const *)a, *(char *const *)b);
}

static void stampa(void *elemento, void *ctx) {
    (void)ctx;
    printf("%s ", (const char *)elemento);
}

int main(void) {
    Vettore *parole = vettore_new(free);
    if (parole == NULL) return 1;

    char buf[64];
    while (scanf("%63s", buf) == 1) {
        char *copia = str_dup(buf);
        if (copia == NULL || !vettore_aggiungi(parole, copia)) {
            free(copia);
            break;
        }
    }

    vettore_ordina(parole, cmp_str);
    for (size_t i = 0; i < vettore_quanti(parole); ) {
        const char *p = vettore_prendi(parole, i);
        if (strlen(p) < 4) vettore_rimuovi(parole, i);     /* i non avanza */
        else i++;
    }

    vettore_per_ognuno(parole, stampa, NULL);
    printf("\n");
    vettore_free(parole);
    return 0;
}
