/*
 * Esercizio 6.7 - Animali
 *
 * L'interfaccia Animale (nome, verso, distruggi) con Cane e Gatto, e un terzo
 * tipo, Cucciolo, che "estende" Cane: ha tutto cio' che ha un cane piu'
 * l'eta' in mesi. La sua tabella riusa la funzione verso del cane e ne
 * sostituisce una sola, nome.
 *
 * L'idea: un Cucciolo comincia con un Cane, che comincia con un Animale. Il
 * primo campo del primo campo sta all'indirizzo dell'oggetto, quindi un
 * Cucciolo * si puo' guardare come Cane * e come Animale *. cane_verso, che
 * converte self in Cane *, lavora su un cucciolo senza saperlo: e'
 * l'ereditarieta', fatta con la disposizione dei campi.
 *
 * Scelte: `nome` restituisce una stringa che vive nell'oggetto (il cucciolo
 * la compone alla creazione, "Fido (3 mesi)"), cosi' chi chiama non deve
 * liberare nulla. Il nome proprio e' copiato in un array dentro l'oggetto.
 *
 * Errore tipico evitato: dichiarare Cucciolo come { Animale base; char
 * nome[32]; int mesi; } ripetendo a mano i campi del cane. Compila, e
 * funziona finche' Cane non cambia; il giorno che a Cane si aggiunge un campo
 * la funzione del cane legge nel cucciolo i dati sbagliati. Con `Cane base`
 * la disposizione e' garantita dal compilatore.
 */
#include <stdio.h>
#include <stdlib.h>

typedef struct Animale Animale;

typedef struct {
    const char *(*nome)(const Animale *self);
    const char *(*verso)(const Animale *self);
    void (*distruggi)(Animale *self);
} AnimaleVT;

struct Animale { const AnimaleVT *vt; };

const char *animale_nome(const Animale *a)  { return a->vt->nome(a); }
const char *animale_verso(const Animale *a) { return a->vt->verso(a); }
void animale_free(Animale *a)               { if (a != NULL) a->vt->distruggi(a); }

static void semplice_distruggi(Animale *self) { free(self); }

/* --- Cane --- */
typedef struct { Animale base; char nome[32]; } Cane;

static const char *cane_nome(const Animale *self)  { return ((const Cane *)self)->nome; }
static const char *cane_verso(const Animale *self) { (void)self; return "bau"; }

static const AnimaleVT CANE_VT = { cane_nome, cane_verso, semplice_distruggi };

/* inizializza la parte Cane di un oggetto gia' allocato: la usa anche Cucciolo */
static void cane_init(Cane *c, const AnimaleVT *vt, const char *nome) {
    c->base.vt = vt;
    snprintf(c->nome, sizeof c->nome, "%s", nome);
}

Animale *cane_new(const char *nome) {
    Cane *c = malloc(sizeof *c);
    if (c == NULL) return NULL;
    cane_init(c, &CANE_VT, nome);
    return &c->base;
}

/* --- Gatto --- */
typedef struct { Animale base; char nome[32]; } Gatto;

static const char *gatto_nome(const Animale *self)  { return ((const Gatto *)self)->nome; }
static const char *gatto_verso(const Animale *self) { (void)self; return "miao"; }

static const AnimaleVT GATTO_VT = { gatto_nome, gatto_verso, semplice_distruggi };

Animale *gatto_new(const char *nome) {
    Gatto *g = malloc(sizeof *g);
    if (g == NULL) return NULL;
    g->base.vt = &GATTO_VT;
    snprintf(g->nome, sizeof g->nome, "%s", nome);
    return &g->base;
}

/* --- Cucciolo: un Cane con l'eta' --- */
typedef struct { Cane base; int mesi; char etichetta[48]; } Cucciolo;

static const char *cucciolo_nome(const Animale *self) {
    return ((const Cucciolo *)self)->etichetta;
}

/* verso: quello del cane, riusato; nome: il suo */
static const AnimaleVT CUCCIOLO_VT = { cucciolo_nome, cane_verso, semplice_distruggi };

Animale *cucciolo_new(const char *nome, int mesi) {
    Cucciolo *k = malloc(sizeof *k);
    if (k == NULL) return NULL;
    cane_init(&k->base, &CUCCIOLO_VT, nome);      /* la parte Cane, con la SUA tabella */
    k->mesi = mesi;
    snprintf(k->etichetta, sizeof k->etichetta, "%s (%d mesi)", k->base.nome, mesi);
    return &k->base.base;                         /* il primo campo del primo campo */
}

int main(void) {
    Animale *zoo[] = { cane_new("Rex"), gatto_new("Micia"), cucciolo_new("Fido", 3) };
    size_t n = sizeof zoo / sizeof zoo[0];
    for (size_t i = 0; i < n; i++) {
        if (zoo[i] == NULL) continue;
        printf("%s fa %s\n", animale_nome(zoo[i]), animale_verso(zoo[i]));
    }
    for (size_t i = 0; i < n; i++) animale_free(zoo[i]);
    return 0;
}
