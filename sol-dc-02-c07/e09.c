/*
 * Esercizio 7.9 - L'agenda e la vista
 *
 * Agenda possiede i contatti: ciascuno e' un blocco con nome e telefono
 * allocati a parte. Vista e' un elenco ordinato di contatti dell'agenda, che
 * li ha IN PRESTITO: contiene puntatori, non copie, e libera solo il proprio
 * array. I contratti sono scritti sopra ogni funzione.
 *
 * Il punto delicato: che cosa succede a una vista quando dall'agenda viene
 * rimosso un contatto? La vista conterrebbe un puntatore a memoria liberata.
 * Scelta: l'agenda tiene un numero di versione che ogni modifica (aggiungi,
 * rimuovi) incrementa; la vista ricorda la versione con cui e' nata e, prima
 * di toccare i contatti, la confronta. Una vista scaduta non stampa e lo
 * dice: l'errore silenzioso (use-after-free) diventa un errore visibile.
 * Per questo la vista tiene in prestito anche l'agenda, che deve vivere piu'
 * a lungo di lei.
 *
 * Costruzioni a meta': contatto_new alloca tre blocchi e, se uno fallisce,
 * libera i precedenti con una sola etichetta (free(NULL) non fa nulla).
 *
 * Errore tipico evitato: vista_free che libera anche i contatti. Sono
 * dell'agenda: li libererebbe due volte (ASan: double free). Chi ha un
 * prestito non libera. Provato con -fsanitize=address.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct { char *nome; char *telefono; } Contatto;

typedef struct {
    Contatto **v;            /* i contatti: l'agenda li possiede */
    size_t n, cap;
    unsigned versione;       /* cambia a ogni modifica */
} Agenda;

typedef enum { PER_NOME, PER_TELEFONO } Criterio;

typedef struct {
    const Agenda *agenda;    /* in prestito: deve vivere piu' della vista */
    const Contatto **v;      /* l'array e' della vista; i contatti sono in prestito */
    size_t n;
    unsigned versione;       /* quella dell'agenda quando la vista e' nata */
} Vista;

static char *str_dup(const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = malloc(n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

static void contatto_free(Contatto *c) {
    if (c == NULL) return;
    free(c->telefono);
    free(c->nome);
    free(c);
}

/* un contatto nuovo, con una COPIA di nome e telefono. E' del chiamante. */
static Contatto *contatto_new(const char *nome, const char *telefono) {
    Contatto *c = calloc(1, sizeof *c);         /* campi a NULL */
    if (c == NULL) return NULL;
    c->nome = str_dup(nome);
    if (c->nome == NULL) goto errore;
    c->telefono = str_dup(telefono);
    if (c->telefono == NULL) goto errore;
    return c;

errore:
    contatto_free(c);                           /* libera cio' che c'e' */
    return NULL;
}

/* un'agenda vuota. E' del chiamante: agenda_free. */
Agenda *agenda_new(void) {
    return calloc(1, sizeof(Agenda));
}

/* aggiunge un contatto copiando nome e telefono (in prestito). Invalida le viste. */
bool agenda_aggiungi(Agenda *a, const char *nome, const char *telefono) {
    if (a->n == a->cap) {
        size_t cap = a->cap == 0 ? 4 : a->cap * 2;
        Contatto **v = realloc(a->v, cap * sizeof *v);
        if (v == NULL) return false;
        a->v = v;
        a->cap = cap;
    }
    Contatto *c = contatto_new(nome, telefono);
    if (c == NULL) return false;
    a->v[a->n++] = c;
    a->versione++;
    return true;
}

/* toglie e libera il contatto con quel nome; false se non c'e'. Invalida le viste. */
bool agenda_rimuovi(Agenda *a, const char *nome) {
    for (size_t i = 0; i < a->n; i++) {
        if (strcmp(a->v[i]->nome, nome) != 0) continue;
        contatto_free(a->v[i]);
        memmove(&a->v[i], &a->v[i + 1], (a->n - i - 1) * sizeof a->v[0]);
        a->n--;
        a->versione++;
        return true;
    }
    return false;
}

/* libera i contatti e l'agenda. Le viste ancora in giro non vanno piu' usate. */
void agenda_free(Agenda *a) {
    if (a == NULL) return;
    for (size_t i = 0; i < a->n; i++) contatto_free(a->v[i]);
    free(a->v);
    free(a);
}

static int cmp_nome(const void *x, const void *y) {
    const Contatto *a = *(const Contatto *const *)x, *b = *(const Contatto *const *)y;
    return strcmp(a->nome, b->nome);
}

static int cmp_telefono(const void *x, const void *y) {
    const Contatto *a = *(const Contatto *const *)x, *b = *(const Contatto *const *)y;
    return strcmp(a->telefono, b->telefono);
}

/* una vista ordinata dei contatti di a, che restano di a (prestito). La vista
   e' del chiamante: vista_free. Vale finche' l'agenda non viene modificata. */
Vista *vista_new(const Agenda *a, Criterio criterio) {
    Vista *v = calloc(1, sizeof *v);
    if (v == NULL) return NULL;
    v->v = malloc((a->n > 0 ? a->n : 1) * sizeof *v->v);
    if (v->v == NULL) { free(v); return NULL; }
    for (size_t i = 0; i < a->n; i++) v->v[i] = a->v[i];
    v->n = a->n;
    v->agenda = a;
    v->versione = a->versione;
    qsort(v->v, v->n, sizeof v->v[0], criterio == PER_NOME ? cmp_nome : cmp_telefono);
    return v;
}

bool vista_valida(const Vista *v) { return v->versione == v->agenda->versione; }

/* stampa i contatti nell'ordine della vista; false (e un avviso) se e' scaduta */
bool vista_stampa(const Vista *v) {
    if (!vista_valida(v)) {
        printf("  (vista scaduta: l'agenda e' cambiata)\n");
        return false;
    }
    for (size_t i = 0; i < v->n; i++)
        printf("  %-10s %s\n", v->v[i]->nome, v->v[i]->telefono);
    return true;
}

/* libera la vista e il suo array; i contatti NON sono suoi */
void vista_free(Vista *v) {
    if (v == NULL) return;
    free(v->v);
    free(v);
}

int main(void) {
    Agenda *a = agenda_new();
    if (a == NULL) return 1;
    bool ok = agenda_aggiungi(a, "Nami", "333-1111")
           && agenda_aggiungi(a, "Greta", "347-9999")
           && agenda_aggiungi(a, "Max", "320-5555")
           && agenda_aggiungi(a, "Loretta", "339-0000");
    if (!ok) { agenda_free(a); return 1; }

    Vista *per_nome = vista_new(a, PER_NOME);
    Vista *per_telefono = vista_new(a, PER_TELEFONO);
    if (per_nome == NULL || per_telefono == NULL) {
        vista_free(per_nome); vista_free(per_telefono); agenda_free(a);
        return 1;
    }
    printf("per nome:\n");      vista_stampa(per_nome);
    printf("per telefono:\n");  vista_stampa(per_telefono);

    agenda_rimuovi(a, "Max");                   /* le due viste scadono */
    printf("dopo la rimozione di Max, la vecchia vista:\n");
    vista_stampa(per_nome);

    vista_free(per_nome);                       /* si rifa': una vista costa poco */
    per_nome = vista_new(a, PER_NOME);
    if (per_nome != NULL) { printf("la vista nuova:\n"); vista_stampa(per_nome); }

    vista_free(per_nome);                       /* prima le viste, poi l'agenda */
    vista_free(per_telefono);
    agenda_free(a);
    return 0;
}
