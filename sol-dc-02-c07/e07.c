/*
 * Esercizio 7.7 - StrBuf
 *
 * StrBuf e' una stringa che cresce in coda: aggiungi, aggiungi_c (un
 * carattere), aggiungi_f (con formato), prendi (consegna la stringa al
 * chiamante), libera. Con questo si scrivono unisci(sep, parole, n) e
 * ripeti(s, volte), che restituiscono stringhe nuove di cui il chiamante e'
 * proprietario.
 *
 * Scelte: uno StrBuf azzerato ({0}) e' un buffer vuoto valido, quindi non
 * serve un costruttore e puo' stare sullo stack. Tutte le aggiunte passano da
 * sb_assicura, che raddoppia la capacita' finche' basta. Le funzioni di
 * aggiunta restituiscono false se manca memoria, e lasciano il buffer com'era.
 * unisci e ripeti raccolgono gli esiti e, se qualcosa e' fallito, liberano e
 * restituiscono NULL: niente stringhe a meta'.
 *
 * Errore tipico evitato: sb_prendi che restituisce sb->s lasciando il buffer
 * com'e'. Il buffer continuerebbe a credere sua una stringa che ha
 * consegnato, e un sb_libera successivo la libererebbe una seconda volta.
 * Dopo la consegna il buffer torna a {0}: vuoto e riusabile. E un buffer mai
 * usato consegna comunque una stringa vuota allocata, non NULL, cosi' chi
 * riceve puo' sempre chiamare free.
 */
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct { char *s; size_t n, cap; } StrBuf;

static bool sb_assicura(StrBuf *sb, size_t k) {
    if (sb->n + k + 1 <= sb->cap) return true;
    size_t cap = sb->cap == 0 ? 16 : sb->cap;
    while (cap < sb->n + k + 1) cap *= 2;
    char *s = realloc(sb->s, cap);
    if (s == NULL) return false;
    sb->s = s;
    sb->cap = cap;
    return true;
}

bool sb_aggiungi(StrBuf *sb, const char *testo) {
    size_t k = strlen(testo);
    if (!sb_assicura(sb, k)) return false;
    memcpy(sb->s + sb->n, testo, k + 1);
    sb->n += k;
    return true;
}

bool sb_aggiungi_c(StrBuf *sb, char c) {
    if (!sb_assicura(sb, 1)) return false;
    sb->s[sb->n++] = c;
    sb->s[sb->n] = '\0';
    return true;
}

__attribute__((format(printf, 2, 3)))
bool sb_aggiungi_f(StrBuf *sb, const char *fmt, ...) {
    va_list ap, ap2;
    va_start(ap, fmt);
    va_copy(ap2, ap);
    int k = vsnprintf(NULL, 0, fmt, ap2);          /* quanto e' lungo il pezzo */
    va_end(ap2);
    bool ok = k >= 0 && sb_assicura(sb, (size_t)k);
    if (ok) {
        vsnprintf(sb->s + sb->n, (size_t)k + 1, fmt, ap);   /* scritto direttamente in coda */
        sb->n += (size_t)k;
    }
    va_end(ap);
    return ok;
}

/* consegna la stringa (free) e lascia il buffer vuoto; NULL solo se manca memoria */
char *sb_prendi(StrBuf *sb) {
    char *s = sb->s;
    if (s == NULL) {
        s = malloc(1);
        if (s != NULL) s[0] = '\0';
    }
    *sb = (StrBuf){ 0 };
    return s;
}

void sb_libera(StrBuf *sb) {
    free(sb->s);
    *sb = (StrBuf){ 0 };
}

/* le parole separate da sep. Il risultato e' del chiamante (free); NULL se manca memoria */
char *unisci(const char *sep, const char *parole[], size_t n) {
    StrBuf sb = { 0 };
    bool ok = true;
    for (size_t i = 0; i < n && ok; i++) {
        if (i > 0) ok = sb_aggiungi(&sb, sep);
        ok = ok && sb_aggiungi(&sb, parole[i]);
    }
    if (!ok) { sb_libera(&sb); return NULL; }
    return sb_prendi(&sb);
}

/* s ripetuta `volte` volte. Il risultato e' del chiamante (free) */
char *ripeti(const char *s, size_t volte) {
    StrBuf sb = { 0 };
    for (size_t i = 0; i < volte; i++)
        if (!sb_aggiungi(&sb, s)) { sb_libera(&sb); return NULL; }
    return sb_prendi(&sb);
}

int main(void) {
    const char *nomi[] = { "Greta", "Loretta", "Nami", "Max" };
    char *a = unisci(", ", nomi, 4);
    char *b = unisci(", ", nomi, 0);            /* nessuna parola: stringa vuota */
    char *c = ripeti("ab", 5);
    if (a == NULL || b == NULL || c == NULL) { free(a); free(b); free(c); return 1; }
    printf("[%s]\n[%s]\n[%s]\n", a, b, c);
    free(a); free(b); free(c);

    StrBuf sb = { 0 };
    for (int i = 1; i <= 5; i++) {
        sb_aggiungi_f(&sb, "%d", i * i);
        sb_aggiungi_c(&sb, i < 5 ? '+' : '=');
    }
    sb_aggiungi_f(&sb, "%d", 1 + 4 + 9 + 16 + 25);
    printf("%s (n = %zu, cap = %zu)\n", sb.s, sb.n, sb.cap);
    sb_libera(&sb);
    sb_libera(&sb);                             /* due volte: innocuo, e' vuoto */
    return 0;
}
