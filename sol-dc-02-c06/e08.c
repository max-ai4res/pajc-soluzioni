/*
 * Esercizio 6.8 - L'Uscita da capo
 *
 * L'interfaccia Uscita (scrivi, chiudi) con tre implementazioni: console,
 * file, e "numerata", che avvolge un'altra uscita e premette a ogni riga il
 * suo numero. uscita_printf compone il testo con un formato, con il controllo
 * del compilatore. Il main scrive le stesse dieci righe su una console
 * numerata e su un file numerato (righe.txt).
 *
 * Scelte: console e file condividono tipo e tabella (UscitaFile): cambia solo
 * se alla chiusura il FILE * va chiuso. La numerata POSSIEDE l'uscita che
 * avvolge: chiudere lei chiude anche l'altra, e chi l'ha creata non deve piu'
 * toccare l'interna. E' un decoratore: ha la stessa interfaccia di cio' che
 * avvolge, quindi si puo' numerare qualunque uscita, anche un'altra numerata.
 *
 * Errore tipico evitato: premettere il numero a ogni chiamata di scrivi. Il
 * testo arriva a pezzi: una riga puo' arrivare in tre chiamate, o tre righe
 * in una. La numerata ricorda se si trova a inizio riga e scorre il testo
 * carattere per carattere: il numero esce quando comincia una riga nuova.
 */
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Uscita Uscita;

typedef struct {
    void (*scrivi)(Uscita *self, const char *testo);
    void (*chiudi)(Uscita *self);
} UscitaVT;

struct Uscita { const UscitaVT *vt; };

void uscita_scrivi(Uscita *u, const char *testo) { u->vt->scrivi(u, testo); }
void uscita_chiudi(Uscita *u) { if (u != NULL) u->vt->chiudi(u); }

__attribute__((format(printf, 2, 3)))
void uscita_printf(Uscita *u, const char *fmt, ...) {
    char testo[512];
    va_list ap;
    va_start(ap, fmt);
    vsnprintf(testo, sizeof testo, fmt, ap);
    va_end(ap);
    uscita_scrivi(u, testo);
}

/* --- console e file --- */
typedef struct { Uscita base; FILE *f; bool mio; } UscitaFile;

static void file_scrivi(Uscita *self, const char *testo) {
    fputs(testo, ((UscitaFile *)self)->f);
}
static void file_chiudi(Uscita *self) {
    UscitaFile *u = (UscitaFile *)self;
    if (u->mio) fclose(u->f);
    free(u);
}
static const UscitaVT FILE_VT = { file_scrivi, file_chiudi };

static Uscita *uscita_su(FILE *f, bool mio) {
    UscitaFile *u = malloc(sizeof *u);
    if (u == NULL) return NULL;
    *u = (UscitaFile){ .base = { &FILE_VT }, .f = f, .mio = mio };
    return &u->base;
}

Uscita *uscita_console(void) { return uscita_su(stdout, false); }

Uscita *uscita_file(const char *percorso) {
    FILE *f = fopen(percorso, "w");
    if (f == NULL) return NULL;
    Uscita *u = uscita_su(f, true);
    if (u == NULL) fclose(f);
    return u;
}

/* --- numerata: avvolge un'altra uscita, che possiede --- */
typedef struct { Uscita base; Uscita *interna; int riga; bool inizio; } UscitaNumerata;

static void numerata_scrivi(Uscita *self, const char *testo) {
    UscitaNumerata *u = (UscitaNumerata *)self;
    for (const char *p = testo; *p != '\0'; p++) {
        if (u->inizio) {
            uscita_printf(u->interna, "%3d: ", ++u->riga);
            u->inizio = false;
        }
        char c[2] = { *p, '\0' };
        uscita_scrivi(u->interna, c);
        if (*p == '\n') u->inizio = true;
    }
}

static void numerata_chiudi(Uscita *self) {
    UscitaNumerata *u = (UscitaNumerata *)self;
    uscita_chiudi(u->interna);
    free(u);
}
static const UscitaVT NUMERATA_VT = { numerata_scrivi, numerata_chiudi };

/* prende possesso di `interna`; se fallisce la chiude e restituisce NULL */
Uscita *uscita_numerata(Uscita *interna) {
    if (interna == NULL) return NULL;
    UscitaNumerata *u = malloc(sizeof *u);
    if (u == NULL) { uscita_chiudi(interna); return NULL; }
    *u = (UscitaNumerata){ .base = { &NUMERATA_VT }, .interna = interna,
                           .riga = 0, .inizio = true };
    return &u->base;
}

static void dieci_righe(Uscita *u) {
    for (int i = 1; i <= 10; i++) {
        uscita_printf(u, "riga ");              /* una riga in tre pezzi */
        uscita_printf(u, "numero %d", i);
        uscita_scrivi(u, "\n");
    }
}

int main(void) {
    Uscita *schermo = uscita_numerata(uscita_console());
    Uscita *file = uscita_numerata(uscita_file("righe.txt"));
    if (schermo == NULL || file == NULL) {
        uscita_chiudi(schermo);
        uscita_chiudi(file);
        return 1;
    }
    dieci_righe(schermo);
    dieci_righe(file);
    uscita_chiudi(schermo);
    uscita_chiudi(file);
    return 0;
}
