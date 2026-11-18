/* testo: una media variadica e le stringhe temporanee del rapporto */
#include "e06_testo.h"

#include <stdarg.h>
#include <stdio.h>

double testo_media(int n, ...) {
    va_list ap;
    va_start(ap, n);
    double somma = 0;
    for (int i = 0; i < n; i++) somma += va_arg(ap, double);
    va_end(ap);
    return n > 0 ? somma / n : 0;
}

/* un'arena minima: un array statico e quanti byte ne sono presi */
static char arena[256];
static size_t usati = 0;

const char *testo_temp(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    char *qui = arena + usati;
    size_t liberi = sizeof arena - usati;
    int n = vsnprintf(qui, liberi, fmt, ap);
    va_end(ap);
    if (n < 0 || (size_t)n >= liberi) return NULL;      /* non ci sta */
    usati += (size_t)n + 1;
    return qui;
}

void testo_azzera(void) { usati = 0; }
