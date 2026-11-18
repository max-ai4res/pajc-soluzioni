/*
 * Esercizio 5.9 - La rubrica a dimensione fissa
 *
 * Un array di MAX_CONTATTI Contatto con la lunghezza n a parte, tenuto in
 * ordine di nome: rubrica_inserisci cerca la prima posizione con un nome che
 * viene dopo, fa scorrere in avanti con memmove i *n - i contatti da li' in
 * poi e scrive nel buco; rubrica_rimuovi cerca il contatto e chiude il buco
 * facendo scorrere all'indietro i *n - i - 1 contatti che lo seguono (non
 * *n - i: l'ultimo scorrerebbe da fuori dall'array). rubrica_cerca e' quella
 * del capitolo sui puntatori; contatto_imposta copia con snprintf, cosi' un
 * nome troppo lungo viene tagliato e non trabocca.
 *
 * Il main legge da stdin righe "nome telefono" fino a "fine" (il telefono e'
 * il resto della riga, puo' contenere spazi: "Greta 030 1234567"), poi i
 * comandi "cerca nome", "rimuovi nome", "stampa", "esci"; si ferma anche a
 * EOF, cosi' funziona da pipe. Quando la rubrica e' piena lo dice e continua.
 */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

typedef struct {
    char nome[32];
    char telefono[16];
} Contatto;

enum { MAX_CONTATTI = 100 };

void contatto_imposta(Contatto *c, const char *nome, const char *telefono) {
    snprintf(c->nome, sizeof c->nome, "%s", nome);
    snprintf(c->telefono, sizeof c->telefono, "%s", telefono);
}

/* l'indirizzo del contatto con quel nome, o NULL */
Contatto *rubrica_cerca(Contatto *v, size_t n, const char *nome) {
    for (size_t i = 0; i < n; i++)
        if (strcmp(v[i].nome, nome) == 0) return &v[i];
    return NULL;
}

/* inserisce in ordine di nome; false se la rubrica e' piena */
bool rubrica_inserisci(Contatto v[], size_t *n, const Contatto *c) {
    if (*n == MAX_CONTATTI) return false;
    size_t i = 0;
    while (i < *n && strcmp(v[i].nome, c->nome) < 0) i++;
    memmove(&v[i + 1], &v[i], (*n - i) * sizeof v[0]);   /* scorri avanti */
    v[i] = *c;
    (*n)++;
    return true;
}

/* toglie il contatto con quel nome chiudendo il buco; false se non c'e' */
bool rubrica_rimuovi(Contatto v[], size_t *n, const char *nome) {
    Contatto *c = rubrica_cerca(v, *n, nome);
    if (c == NULL) return false;
    size_t i = (size_t)(c - v);                           /* l'indice da &v[i] */
    memmove(&v[i], &v[i + 1], (*n - i - 1) * sizeof v[0]); /* scorri indietro */
    (*n)--;
    return true;
}

void rubrica_stampa(const Contatto v[], size_t n) {
    if (n == 0) { printf("(rubrica vuota)\n"); return; }
    for (size_t i = 0; i < n; i++)
        printf("%zu %-10s %s\n", i, v[i].nome, v[i].telefono);
}

/* separa la prima parola (il nome o il comando) dal resto della riga */
static void spezza(char *riga, char **prima, char **resto) {
    riga[strcspn(riga, "\n")] = '\0';
    while (*riga == ' ') riga++;
    *prima = riga;
    while (*riga != '\0' && *riga != ' ') riga++;
    if (*riga != '\0') { *riga = '\0'; riga++; }
    while (*riga == ' ') riga++;
    *resto = riga;
}

int main(void) {
    Contatto rubrica[MAX_CONTATTI];
    size_t n = 0;
    char riga[128];
    char *prima, *resto;

    printf("Contatti (nome telefono), poi \"fine\":\n");
    while (fgets(riga, sizeof riga, stdin) != NULL) {
        spezza(riga, &prima, &resto);
        if (strcmp(prima, "fine") == 0) break;
        if (*prima == '\0') continue;                      /* riga vuota */
        Contatto c;
        contatto_imposta(&c, prima, resto);
        if (!rubrica_inserisci(rubrica, &n, &c))
            printf("rubrica piena (%d contatti): %s non inserito\n",
                   MAX_CONTATTI, c.nome);
    }

    printf("Comandi: cerca nome | rimuovi nome | stampa | esci\n");
    while (fgets(riga, sizeof riga, stdin) != NULL) {
        spezza(riga, &prima, &resto);
        if (strcmp(prima, "esci") == 0) break;
        if (strcmp(prima, "stampa") == 0) {
            rubrica_stampa(rubrica, n);
        } else if (strcmp(prima, "cerca") == 0) {
            const Contatto *c = rubrica_cerca(rubrica, n, resto);
            if (c != NULL) printf("%s: %s\n", c->nome, c->telefono);
            else printf("%s: non in rubrica\n", resto);
        } else if (strcmp(prima, "rimuovi") == 0) {
            if (rubrica_rimuovi(rubrica, &n, resto)) printf("%s rimosso\n", resto);
            else printf("%s: non in rubrica\n", resto);
        } else if (*prima != '\0') {
            printf("comando sconosciuto: %s\n", prima);
        }
    }
    return 0;
}
