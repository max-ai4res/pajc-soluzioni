/*
 * Esercizio 5.8 - Parole e tagli
 *
 * conta_parole scorre la stringa e conta le transizioni spazio -> non spazio:
 * cosi' uno o piu' spazi contano come un separatore solo, e gli spazi in
 * testa o in coda non creano parole. taglia mette '\0' in s[max] solo se la
 * stringa e' piu' lunga di max; se e' gia' piu' corta non fa nulla.
 * ultima_parola torna indietro dalla fine saltando gli spazi di coda, poi
 * risale fino allo spazio che precede la parola: restituisce un puntatore
 * DENTRO s, non una copia, e NULL se non ci sono parole.
 *
 * Il parametro e' const char * ma il risultato e' char *: e' la stessa
 * convenzione di strchr e strstr della libreria, che chiede un cast in un
 * punto solo e documentato (il chiamante decide se scrivere nella sua stringa).
 * Errore evitato: fgets lascia il '\n' nel buffer, e senza toglierlo
 * "ciao mondo\n" avrebbe una ultima parola che finisce con l'a capo;
 * s[strcspn(s, "\n")] = '\0' lo toglie prima di tutto.
 */
#include <ctype.h>
#include <stdio.h>
#include <string.h>

size_t conta_parole(const char *s) {
    size_t n = 0;
    int dentro = 0;                           /* siamo dentro una parola? */
    for (; *s != '\0'; s++) {
        if (*s == ' ') dentro = 0;
        else if (!dentro) { dentro = 1; n++; }
    }
    return n;
}

void taglia(char *s, size_t max) {
    if (strlen(s) > max) s[max] = '\0';
}

char *ultima_parola(const char *s) {
    size_t i = strlen(s);
    while (i > 0 && s[i - 1] == ' ') i--;     /* salta gli spazi di coda */
    if (i == 0) return NULL;                  /* solo spazi, o vuota */
    while (i > 0 && s[i - 1] != ' ') i--;     /* risali all'inizio della parola */
    return (char *)(s + i);                   /* dentro s, come strchr */
}

static void prova(const char *originale) {
    char s[256];
    snprintf(s, sizeof s, "%s", originale);
    printf("\"%s\"\n", s);
    printf("  parole: %zu\n", conta_parole(s));
    const char *u = ultima_parola(s);
    printf("  ultima parola: %s\n", u != NULL ? u : "(nessuna)");
    taglia(s, 10);
    printf("  tagliata a 10: \"%s\"\n", s);
}

int main(void) {
    /* attese: 2 parole / mondo; 3 parole / Nami; 0 parole / nessuna; 1 / ciao */
    prova("ciao mondo");
    prova("  Greta   Loretta  Nami  ");
    prova("     ");
    prova("ciao");

    printf("\nScrivi una riga (Ctrl-D per finire):\n");
    char riga[256];
    while (fgets(riga, sizeof riga, stdin) != NULL) {
        riga[strcspn(riga, "\n")] = '\0';     /* via il '\n' di fgets */
        prova(riga);
    }
    return 0;
}
