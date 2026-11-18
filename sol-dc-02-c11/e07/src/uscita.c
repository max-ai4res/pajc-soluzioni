/* uscita: dove vanno le risposte. Un'interfaccia e quattro implementazioni. */
#include "uscita.h"

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "str.h"

void uscita_scrivi(Uscita *u, const char *testo) { u->vt->scrivi(u, testo); }
void uscita_chiudi(Uscita *u) { if (u != NULL) u->vt->chiudi(u); }

void uscita_vprintf(Uscita *u, const char *fmt, va_list ap) {
    char *testo = str_vprintf(fmt, ap);
    if (testo == NULL) return;
    uscita_scrivi(u, testo);
    free(testo);
}

/* --- su un FILE *: lo schermo, o un file aperto da noi --- */
typedef struct { Uscita base; FILE *f; bool mio; } UscitaFile;

static void file_scrivi(Uscita *self, const char *testo) {
    UscitaFile *u = (UscitaFile *)self;
    fputs(testo, u->f);
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

/* --- in memoria: una stringa che cresce --- */
typedef struct { Uscita base; char *testo; size_t n, cap; } UscitaMemoria;

static void memoria_scrivi(Uscita *self, const char *testo) {
    UscitaMemoria *u = (UscitaMemoria *)self;
    size_t k = strlen(testo);
    if (u->n + k + 1 > u->cap) {
        size_t cap = u->cap == 0 ? 64 : u->cap;
        while (cap < u->n + k + 1) cap *= 2;
        char *t = realloc(u->testo, cap);
        if (t == NULL) return;
        u->testo = t;
        u->cap = cap;
    }
    memcpy(u->testo + u->n, testo, k + 1);
    u->n += k;
}

static void memoria_chiudi(Uscita *self) {
    UscitaMemoria *u = (UscitaMemoria *)self;
    free(u->testo);
    free(u);
}

static const UscitaVT MEMORIA_VT = { memoria_scrivi, memoria_chiudi };

Uscita *uscita_memoria(void) {
    UscitaMemoria *u = malloc(sizeof *u);
    if (u == NULL) return NULL;
    *u = (UscitaMemoria){ .base = { &MEMORIA_VT }, .testo = NULL, .n = 0, .cap = 0 };
    return &u->base;
}

const char *uscita_memoria_testo(const Uscita *u) {
    const UscitaMemoria *m = (const UscitaMemoria *)u;
    return m->testo == NULL ? "" : m->testo;
}

/* --- doppia: inoltra ad altre due, che possiede --- */
typedef struct { Uscita base; Uscita *a, *b; } UscitaDoppia;

static void doppia_scrivi(Uscita *self, const char *testo) {
    UscitaDoppia *u = (UscitaDoppia *)self;
    uscita_scrivi(u->a, testo);
    uscita_scrivi(u->b, testo);
}

static void doppia_chiudi(Uscita *self) {
    UscitaDoppia *u = (UscitaDoppia *)self;
    uscita_chiudi(u->a);
    uscita_chiudi(u->b);
    free(u);
}

static const UscitaVT DOPPIA_VT = { doppia_scrivi, doppia_chiudi };

Uscita *uscita_doppia(Uscita *a, Uscita *b) {
    UscitaDoppia *u = malloc(sizeof *u);
    if (u == NULL) return NULL;
    *u = (UscitaDoppia){ .base = { &DOPPIA_VT }, .a = a, .b = b };
    return &u->base;
}
