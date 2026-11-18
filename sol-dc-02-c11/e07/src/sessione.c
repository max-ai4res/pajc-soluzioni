/* sessione: riceve le righe di testo e le esegue */
#include "sessione.h"

#include <assert.h>
#include <stdalign.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include "registro.h"
#include "str.h"
#include "vettore.h"

#define MAX_ARG 2
#define ARRAY_LEN(a) (sizeof(a) / sizeof((a)[0]))

/* ---- l'arena: la memoria che dura una riga. Serve solo qui. ---- */
typedef struct { char *base; size_t usati, cap; } Arena;

static bool arena_init(Arena *a, size_t cap) {
    a->base = malloc(cap);
    a->usati = 0;
    a->cap = a->base == NULL ? 0 : cap;
    return a->base != NULL;
}

static void *arena_alloc(Arena *a, size_t n) {
    size_t passo = alignof(max_align_t);
    n = (n + passo - 1) / passo * passo;
    if (n > a->cap - a->usati) return NULL;
    void *p = a->base + a->usati;
    a->usati += n;
    return p;
}

static char *arena_str(Arena *a, const char *s) {
    size_t n = strlen(s) + 1;
    char *copia = arena_alloc(a, n);
    if (copia != NULL) memcpy(copia, s, n);
    return copia;
}

/* ---- la sessione ---- */
struct Sessione {
    Registro *registro;          /* suo */
    Vettore *storia;             /* di char *, le possiede */
    Utente *io;                  /* in prestito dal registro; NULL prima di /nome */
    Uscita *uscita;              /* in prestito: la chiude chi l'ha creata */
    Arena arena;                 /* la memoria che dura una riga */
    bool finita;
};

/* una riga della tabella: argomenti attesi, testo per /aiuto, che cosa fare */
typedef struct {
    const char *nome;
    int argomenti;
    const char *aiuto;
    void (*esegui)(Sessione *s, char *arg[]);
} Comando;

/* l'elenco dei comandi, scritto una volta: nome, argomenti, testo per /aiuto.
   In ordine alfabetico, perche' sessione_elabora li cerca con bsearch. */
#define COMANDI_X                                                       \
    X(a,      2, "/a N testo  privato a N")                             \
    X(aiuto,  0, "/aiuto      questo elenco")                           \
    X(chi,    0, "/chi        chi e' collegato")                        \
    X(esci,   0, "/esci       saluta")                                  \
    X(nome,   1, "/nome N     il nome, per primo")                      \
    X(ordina, 1, "/ordina C   ordina /chi: nome, messaggi")             \
    X(storia, 0, "/storia     i messaggi scritti finora")

/* primo uso dell'elenco: i prototipi delle funzioni cmd_* */
#define X(nome, argomenti, aiuto) static void cmd_##nome(Sessione *, char *[]);
COMANDI_X
#undef X

/* secondo uso: le righe della tabella */
static const Comando COMANDI[] = {
#define X(nome, argomenti, aiuto) { "/" #nome, argomenti, aiuto, cmd_##nome },
    COMANDI_X
#undef X
};

_Static_assert(MAX_ARG >= 2, "/a vuole due argomenti: MAX_ARG deve bastare");

/* la tabella e' davvero in ordine? lo controlla un assert alla creazione.
   Con NDEBUG l'assert sparisce, e con lui deve sparire la funzione che usa. */
#ifndef NDEBUG
static bool comandi_in_ordine(void) {
    for (size_t i = 1; i < ARRAY_LEN(COMANDI); i++)
        if (strcmp(COMANDI[i - 1].nome, COMANDI[i].nome) >= 0) return false;
    return true;
}
#endif

__attribute__((format(printf, 2, 3)))
static void rispondi(Sessione *s, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    uscita_vprintf(s->uscita, fmt, ap);
    va_end(ap);
}

/* aggiunge alla storia la riga "nome testo"; nome e testo sono in prestito */
static void ricorda(Sessione *s, const char *nome, const char *testo) {
    char *riga = str_printf("%s %s", nome, testo);
    if (riga == NULL || !vettore_aggiungi(s->storia, riga)) free(riga);
}

static void cmd_nome(Sessione *s, char *arg[]) {
    Utente *u = registro_cerca(s->registro, arg[0]);
    if (u == NULL) u = registro_aggiungi(s->registro, arg[0]);
    if (u == NULL) { rispondi(s, "ERR memoria esaurita\n"); return; }
    s->io = u;
    rispondi(s, "BENVENUTO %s\n", utente_nome(u));
}

static void stampa_nome(const Utente *u, void *ctx) {
    rispondi(ctx, " %s", utente_nome(u));
}

static void cmd_chi(Sessione *s, char *arg[]) {
    (void)arg;
    rispondi(s, "UTENTI");
    registro_per_ognuno(s->registro, stampa_nome, s);
    rispondi(s, "\n");
}

static void cmd_ordina(Sessione *s, char *arg[]) {
    if (registro_ordina_per(s->registro, arg[0])) rispondi(s, "INFO ordine per %s\n", arg[0]);
    else rispondi(s, "ERR criterio sconosciuto: %s\n", arg[0]);
}

typedef struct { Sessione *s; int numero; } Elenco;

static void stampa_riga(void *elemento, void *ctx) {
    Elenco *e = ctx;
    rispondi(e->s, "INFO %d %s\n", ++e->numero, (const char *)elemento);
}

static void cmd_storia(Sessione *s, char *arg[]) {
    (void)arg;
    Elenco e = { .s = s, .numero = 0 };
    vettore_per_ognuno(s->storia, stampa_riga, &e);
    if (e.numero == 0) rispondi(s, "INFO nessun messaggio\n");
}

static void cmd_a(Sessione *s, char *arg[]) {
    if (s->io == NULL) { rispondi(s, "ERR prima /nome\n"); return; }
    if (registro_cerca(s->registro, arg[0]) == NULL) {
        rispondi(s, "ERR non c'e': %s\n", arg[0]);
        return;
    }
    utente_ha_scritto(s->io);
    rispondi(s, "PRIV %s %s %s\n", utente_nome(s->io), arg[0], arg[1]);
}

static void cmd_esci(Sessione *s, char *arg[]) {
    (void)arg;
    rispondi(s, "CIAO\n");
    s->finita = true;
}

static void cmd_aiuto(Sessione *s, char *arg[]) {
    (void)arg;
    for (size_t i = 0; i < ARRAY_LEN(COMANDI); i++)
        rispondi(s, "INFO %s\n", COMANDI[i].aiuto);
}

static void messaggio(Sessione *s, const char *testo) {
    if (s->io == NULL) { rispondi(s, "ERR prima /nome\n"); return; }
    utente_ha_scritto(s->io);
    ricorda(s, utente_nome(s->io), testo);
    rispondi(s, "MSG %s %s\n", utente_nome(s->io), testo);
}

/* spezza resto in n argomenti: l'ultimo e' tutto cio' che resta, spazi compresi;
   false se sono meno di n, o se ce ne sono quando n e' zero */
static bool argomenti(char *resto, int n, char *arg[]) {
    for (int i = 0; i < n; i++) {
        resto += strspn(resto, " ");
        if (*resto == '\0') return false;
        arg[i] = resto;
        if (i < n - 1) {
            resto += strcspn(resto, " ");
            if (*resto != '\0') *resto++ = '\0';
        }
    }
    return n > 0 || resto[strspn(resto, " ")] == '\0';
}

/* per bsearch: la chiave e' il nome scritto dall'utente */
static int cmp_nome_comando(const void *chiave, const void *elemento) {
    const Comando *c = elemento;
    return strcmp(chiave, c->nome);
}

void sessione_elabora(Sessione *s, const char *testo) {
    if (testo[0] != '/') { messaggio(s, testo); return; }

    s->arena.usati = 0;                         /* la riga precedente non serve piu' */
    char *riga = arena_str(&s->arena, testo);
    if (riga == NULL) { rispondi(s, "ERR riga troppo lunga\n"); return; }
    char *resto = riga + strcspn(riga, " ");
    if (*resto != '\0') *resto++ = '\0';

    const Comando *c = bsearch(riga, COMANDI, ARRAY_LEN(COMANDI), sizeof COMANDI[0],
                               cmp_nome_comando);
    if (c == NULL) { rispondi(s, "ERR comando sconosciuto: %s\n", riga); return; }
    char *arg[MAX_ARG];
    if (argomenti(resto, c->argomenti, arg)) c->esegui(s, arg);
    else rispondi(s, "ERR uso: %s\n", c->aiuto);
}

bool sessione_finita(const Sessione *s) { return s->finita; }

Sessione *sessione_new(Uscita *u) {
    assert(u != NULL);
    assert(comandi_in_ordine());                /* errore di chi ha scritto la tabella */
    Sessione *s = calloc(1, sizeof *s);         /* tutti i campi a zero */
    if (s == NULL) return NULL;
    s->uscita = u;
    s->registro = registro_new();
    if (s->registro == NULL) goto errore;
    s->storia = vettore_new(free);
    if (s->storia == NULL) goto errore;
    if (!arena_init(&s->arena, 1024)) goto errore;
    return s;

errore:
    sessione_free(s);
    return NULL;
}

void sessione_free(Sessione *s) {
    if (s == NULL) return;
    free(s->arena.base);
    vettore_free(s->storia);
    registro_free(s->registro);
    free(s);
}
