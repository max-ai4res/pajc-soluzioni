/* Esercizio 10.6 - Il debugging con ASan: IL PROGRAMMA CON GLI ERRORI
 * Una lista della spesa con stringhe allocate: aggiungi, rimuovi, cerca, stampa,
 * carica da un file di testo (una voce per riga: nome quantita'), salva.
 * Compila pulito con -std=c17 -Wall -Wextra -Werror e contiene SEI errori di memoria:
 * un puntatore pendente dopo una rimozione, un overflow di un byte in una copia, una
 * strcpy in un buffer troppo corto, un doppio free in un ramo d'errore, un leak quando
 * il file non si apre, un realloc senza variabile d'appoggio.
 * NON correggere questo file: compilarlo con ASan e seguire i messaggi, un errore alla
 * volta, lavorando su una copia; la versione corretta con il diario e' e06.c.
 *     make asan-bug && ./bin/e06_bug e06_spesa.txt
 *     ./bin/e06_bug non_esiste.txt            (il ramo del leak: su macOS, leaks)
 * Senza ASan il programma "funziona" fino al doppio free, che la libreria di sistema
 * intercetta e ferma senza dire file ne' riga (su macOS un trap muto, su Linux
 * "free(): double free detected"): e' il sintomo lontano dalla causa del capitolo 6.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdint.h>

enum { RIGA_MAX = 128, CAP_INIZIALE = 4 };

typedef struct {
    char *nome;             /* allocato: della voce */
    int quantita;
} Voce;

typedef struct {
    Voce *v;
    size_t n, cap;
} Lista;

/* Copia di s sullo heap; il chiamante libera. */
static char *duplica(const char *s) {
    char *d = malloc(strlen(s));            /* manca il byte per lo zero */
    if (d == NULL) return NULL;
    strcpy(d, s);
    return d;
}

bool lista_init(Lista *l, size_t cap) {
    l->v = malloc(cap * sizeof *l->v);
    if (l->v == NULL) return false;
    l->n = 0;
    l->cap = cap;
    return true;
}

static bool lista_cresci(Lista *l) {
    size_t nuova = l->cap * 2;
    l->v = realloc(l->v, nuova * sizeof *l->v);    /* se fallisce, il blocco e' perso */
    if (l->v == NULL) return false;
    l->cap = nuova;
    return true;
}

/* Aggiunge una voce in coda; il nome viene copiato. */
bool lista_aggiungi(Lista *l, const char *nome, int quantita) {
    if (l->n == l->cap && !lista_cresci(l)) return false;
    char *copia = duplica(nome);
    if (copia == NULL) return false;
    l->v[l->n].nome = copia;
    l->v[l->n].quantita = quantita;
    l->n++;
    return true;
}

/* L'indice della voce con quel nome, o SIZE_MAX. */
size_t lista_cerca(const Lista *l, const char *nome) {
    for (size_t i = 0; i < l->n; i++)
        if (strcmp(l->v[i].nome, nome) == 0) return i;
    return SIZE_MAX;
}

/* Toglie la voce con quel nome; false se non c'e'. */
bool lista_rimuovi(Lista *l, const char *nome) {
    size_t i = lista_cerca(l, nome);
    if (i == SIZE_MAX) return false;
    free(l->v[i].nome);
    memmove(&l->v[i], &l->v[i + 1], (l->n - i - 1) * sizeof l->v[0]);
    l->n--;
    return true;
}

void lista_stampa(const Lista *l) {
    for (size_t i = 0; i < l->n; i++) {
        char etichetta[16];
        strcpy(etichetta, l->v[i].nome);        /* il nome puo' essere piu' lungo */
        for (char *c = etichetta; *c != '\0'; c++) *c = (char)toupper((unsigned char)*c);
        printf("  %-32s %3d\n", etichetta, l->v[i].quantita);
    }
}

/* Scrive la lista nel formato del file (una voce per riga). */
void lista_salva(const Lista *l, FILE *out) {
    for (size_t i = 0; i < l->n; i++)
        fprintf(out, "%s %d\n", l->v[i].nome, l->v[i].quantita);
}

/* Separa "nome quantita'": in *nome una copia allocata del nome, in *q la quantita'.
 * Se la riga non e' valida libera cio' che ha allocato e restituisce false. */
static bool analizza_riga(char *riga, char **nome, int *q) {
    char *tok = strtok(riga, " \t");
    if (tok == NULL) return false;
    *nome = duplica(tok);
    if (*nome == NULL) return false;
    char *qs = strtok(NULL, " \t");
    if (qs == NULL) {
        free(*nome);
        return false;
    }
    char *fine;
    long v = strtol(qs, &fine, 10);
    if (*fine != '\0' || v <= 0) {
        free(*nome);
        return false;
    }
    *q = (int)v;
    return true;
}

bool lista_carica(Lista *l, const char *percorso) {
    char *riga = malloc(RIGA_MAX);
    if (riga == NULL) return false;
    FILE *f = fopen(percorso, "r");
    if (f == NULL) {
        perror(percorso);
        return false;                           /* riga non viene liberata */
    }
    size_t n_riga = 0;
    while (fgets(riga, RIGA_MAX, f) != NULL) {
        n_riga++;
        riga[strcspn(riga, "\n")] = '\0';
        if (riga[0] == '\0') continue;
        char *nome = NULL;
        int q = 0;
        if (!analizza_riga(riga, &nome, &q)) {
            fprintf(stderr, "%s:%zu: riga non valida, ignorata\n", percorso, n_riga);
            free(nome);                         /* analizza_riga l'ha gia' liberato */
            continue;
        }
        if (!lista_aggiungi(l, nome, q))
            fprintf(stderr, "%s:%zu: memoria esaurita\n", percorso, n_riga);
        free(nome);                             /* la copia temporanea */
    }
    fclose(f);
    free(riga);
    return true;
}

void lista_free(Lista *l) {
    for (size_t i = 0; i < l->n; i++) free(l->v[i].nome);
    free(l->v);
    l->v = NULL;
    l->n = l->cap = 0;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "uso: %s spesa.txt [salvataggio.txt]\n", argv[0]);
        return 1;
    }
    Lista l;
    if (!lista_init(&l, CAP_INIZIALE)) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    if (!lista_carica(&l, argv[1])) {
        lista_free(&l);
        return 1;
    }
    printf("lista caricata: %zu voci\n", l.n);
    lista_stampa(&l);

    size_t i = lista_cerca(&l, "latte");
    if (i != SIZE_MAX) printf("latte: %d\n", l.v[i].quantita);

    size_t ip = lista_cerca(&l, "pane");
    const char *nome_pane = (ip != SIZE_MAX) ? l.v[ip].nome : "(nessuno)";
    lista_rimuovi(&l, "pane");
    printf("rimosso: %s\n", nome_pane);          /* il nome e' stato liberato */

    lista_aggiungi(&l, "burro", 1);
    printf("dopo rimozione e aggiunta: %zu voci\n", l.n);
    lista_stampa(&l);

    printf("--- salvataggio (formato del file) ---\n");
    lista_salva(&l, stdout);
    if (argc > 2) {
        FILE *out = fopen(argv[2], "w");
        if (out == NULL) {
            perror(argv[2]);
        } else {
            lista_salva(&l, out);
            fclose(out);
            printf("salvata in %s\n", argv[2]);
        }
    }
    lista_free(&l);
    return 0;
}
