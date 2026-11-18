/* comandi: la tabella e le funzioni dei comandi */
#include "comandi.h"

#include <stdio.h>
#include <string.h>

#include "contatto.h"
#include "rubrica.h"

typedef struct {
    const char *nome;
    int argomenti;
    const char *aiuto;
    bool (*esegui)(Rubrica *r, char *arg[]);       /* false: terminare */
} Comando;

static bool cmd_aggiungi(Rubrica *r, char *arg[]);
static bool cmd_cerca(Rubrica *r, char *arg[]);
static bool cmd_togli(Rubrica *r, char *arg[]);
static bool cmd_elenco(Rubrica *r, char *arg[]);
static bool cmd_aiuto(Rubrica *r, char *arg[]);
static bool cmd_fine(Rubrica *r, char *arg[]);

static const Comando COMANDI[] = {
    { "aggiungi", 2, "aggiungi NOME TELEFONO", cmd_aggiungi },
    { "cerca",    1, "cerca NOME",             cmd_cerca    },
    { "togli",    1, "togli NOME",             cmd_togli    },
    { "elenco",   0, "elenco",                 cmd_elenco   },
    { "aiuto",    0, "aiuto",                  cmd_aiuto    },
    { "fine",     0, "fine",                   cmd_fine     },
};
#define N_COMANDI (sizeof COMANDI / sizeof COMANDI[0])
#define MAX_ARG 2

static bool cmd_aggiungi(Rubrica *r, char *arg[]) {
    switch (rubrica_aggiungi(r, contatto_new(arg[0], arg[1]))) {
    case RUBRICA_OK:      printf("aggiunto %s\n", arg[0]); break;
    case RUBRICA_DOPPIO:  printf("%s c'e' gia'\n", arg[0]); break;
    case RUBRICA_MEMORIA: printf("memoria esaurita\n"); break;
    }
    return true;
}

static bool cmd_cerca(Rubrica *r, char *arg[]) {
    const Contatto *c = rubrica_cerca(r, arg[0]);
    if (c == NULL) printf("%s non c'e'\n", arg[0]);
    else printf("%s: %s\n", c->nome, c->telefono);
    return true;
}

static bool cmd_togli(Rubrica *r, char *arg[]) {
    printf(rubrica_togli(r, arg[0]) ? "tolto %s\n" : "%s non c'e'\n", arg[0]);
    return true;
}

static void stampa(const Contatto *c, void *ctx) {
    size_t *numero = ctx;
    printf("%2zu. %-12s %s\n", ++*numero, c->nome, c->telefono);
}

static bool cmd_elenco(Rubrica *r, char *arg[]) {
    (void)arg;
    size_t numero = 0;
    rubrica_per_ognuno(r, stampa, &numero);
    if (numero == 0) printf("la rubrica e' vuota\n");
    return true;
}

static bool cmd_aiuto(Rubrica *r, char *arg[]) {
    (void)r; (void)arg;
    for (size_t i = 0; i < N_COMANDI; i++) printf("  %s\n", COMANDI[i].aiuto);
    return true;
}

static bool cmd_fine(Rubrica *r, char *arg[]) {
    (void)arg;
    printf("fine: %zu contatti\n", rubrica_quanti(r));
    return false;
}

/* spezza la riga in parole, sul posto; restituisce quante sono (al piu' max + 1) */
static int parole(char *riga, char *p[], int max) {
    int n = 0;
    for (char *t = strtok(riga, " \t\n"); t != NULL && n <= max; t = strtok(NULL, " \t\n"))
        p[n++] = t;
    return n;
}

bool comandi_esegui(Rubrica *r, char *riga) {
    char *p[MAX_ARG + 2];
    int n = parole(riga, p, MAX_ARG + 1);
    if (n == 0) return true;                        /* riga vuota */

    for (size_t i = 0; i < N_COMANDI; i++) {
        const Comando *c = &COMANDI[i];
        if (strcmp(p[0], c->nome) != 0) continue;
        if (n - 1 != c->argomenti) { printf("uso: %s\n", c->aiuto); return true; }
        return c->esegui(r, p + 1);
    }
    printf("comando sconosciuto: %s (prova aiuto)\n", p[0]);
    return true;
}
