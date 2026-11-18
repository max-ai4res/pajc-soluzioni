/*
 * Esercizio 5.7 - La mini printf
 *
 * stampa(fmt, ...) capisce %d, %u, %x, %s, %c, %% e restituisce il numero di
 * caratteri scritti, come printf. I numeri sono convertiti in cifre a mano,
 * da scrivi_unsigned: resti della divisione per la base, raccolti in un
 * buffer dal fondo. Il main confronta testo e conteggio con quelli di printf
 * su dieci formati (snprintf in un buffer, e una stampa "in memoria").
 *
 * Scelte: per poter confrontare il testo senza catturare stdout, il cuore e'
 * vstampa(FILE *, fmt, va_list): stampa() la chiama con stdout, la prova con
 * un file temporaneo. E' lo schema "funzione con i puntini + gemella con la
 * v" della libreria.
 *
 * Errore tipico evitato: per %d, scrivere '-' e poi convertire -x. Per
 * INT_MIN l'opposto non e' rappresentabile (overflow, comportamento
 * indefinito). Si passa al tipo senza segno PRIMA di negare: 0u - (unsigned)x
 * e' ben definito e da' il valore assoluto giusto.
 */
#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>

static int scrivi_unsigned(FILE *f, unsigned v, unsigned base) {
    static const char CIFRE[] = "0123456789abcdef";
    char buf[32];
    int n = 0;
    do {
        buf[n++] = CIFRE[v % base];
        v /= base;
    } while (v != 0);
    for (int i = n - 1; i >= 0; i--) fputc(buf[i], f);
    return n;
}

int vstampa(FILE *f, const char *fmt, va_list ap) {
    int scritti = 0;
    for (const char *p = fmt; *p != '\0'; p++) {
        if (*p != '%') { fputc(*p, f); scritti++; continue; }
        switch (*++p) {
        case 'd': {
            int x = va_arg(ap, int);
            unsigned u = (unsigned)x;
            if (x < 0) { fputc('-', f); scritti++; u = 0u - u; }
            scritti += scrivi_unsigned(f, u, 10);
            break;
        }
        case 'u': scritti += scrivi_unsigned(f, va_arg(ap, unsigned), 10); break;
        case 'x': scritti += scrivi_unsigned(f, va_arg(ap, unsigned), 16); break;
        case 's': {
            const char *s = va_arg(ap, const char *);
            fputs(s, f);
            scritti += (int)strlen(s);
            break;
        }
        case 'c': fputc(va_arg(ap, int), f); scritti++; break;
        case '%': fputc('%', f); scritti++; break;
        case '\0': return scritti;                 /* formato che finisce con % */
        default:  fputc('?', f); scritti++; break;
        }
    }
    return scritti;
}

__attribute__((format(printf, 1, 2)))
int stampa(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int n = vstampa(stdout, fmt, ap);
    va_end(ap);
    return n;
}

/* per la prova: scrive in un file temporaneo e rilegge in buf */
__attribute__((format(printf, 3, 4)))
static int stampa_in(char *buf, size_t cap, const char *fmt, ...) {
    FILE *f = tmpfile();
    if (f == NULL) return -1;
    va_list ap;
    va_start(ap, fmt);
    int n = vstampa(f, fmt, ap);
    va_end(ap);
    rewind(f);
    size_t letti = fread(buf, 1, cap - 1, f);
    buf[letti] = '\0';
    fclose(f);
    return n;
}

#define PROVA(...)                                                          \
    do {                                                                    \
        char a_[128], b_[128];                                              \
        int na_ = stampa_in(a_, sizeof a_, __VA_ARGS__);                    \
        int nb_ = snprintf(b_, sizeof b_, __VA_ARGS__);                     \
        int ok_ = na_ == nb_ && strcmp(a_, b_) == 0;                        \
        printf("%s %2d \"%s\"\n", ok_ ? "ok  " : "DIVERSO", na_, a_);       \
        if (!ok_) errori++;                                                 \
    } while (0)

int main(void) {
    int errori = 0;
    PROVA("ciao");
    PROVA("%d", 0);
    PROVA("%d e %d", 42, -42);
    PROVA("%d", INT_MIN);
    PROVA("%d", INT_MAX);
    PROVA("%u", UINT_MAX);
    PROVA("%x %x", 255u, 0xcafebabeu);
    PROVA("%s ha %d anni", "Greta", 20);
    PROVA("[%c%c%c]", 'a', 'b', 'c');
    PROVA("100%%");
    stampa("%s: %d errori\n", "totale", errori);
    return errori != 0;
}
