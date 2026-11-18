/*
 * Esercizio 5.6 - Tre funzioni su stringhe
 *
 * reverse rovescia la stringa sul posto con due indici che si vengono
 * incontro (nessun array d'appoggio); is_palindrome non modifica nulla,
 * confronta i caratteri simmetrici e si ferma al primo diverso; freq conta
 * le lettere ignorando maiuscole e caratteri non alfabetici, con
 * tolower/isalpha di ctype.h e l'indice c - 'a'.
 *
 * Errore evitato: reverse si prova su un array (char s[] = "ciao"), mai su
 * un letterale, che sta in memoria di sola lettura; e is_palindrome, con il
 * parametro const, confronta invece di chiamare reverse sul proprio argomento.
 * Il main prova prima qualche stringa fissa, poi le righe lette da stdin fino
 * a EOF (funziona anche da pipe: printf 'radar\n' | ./bin/e06).
 */
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

/* rovescia s sul posto: due indici, uno dall'inizio e uno dalla fine */
void reverse(char *s) {
    size_t len = strlen(s);
    if (len < 2) return;
    size_t i = 0, j = len - 1;
    while (i < j) {
        char t = s[i];
        s[i] = s[j];
        s[j] = t;
        i++;
        j--;
    }
}

/* vero se s si legge uguale nei due versi; non tocca la stringa */
bool is_palindrome(const char *s) {
    size_t len = strlen(s);
    for (size_t i = 0; i < len / 2; i++)
        if (s[i] != s[len - 1 - i]) return false;
    return true;
}

/* conta[k] = quante volte compare la k-esima lettera (a = 0, ..., z = 25) */
void freq(const char *s, size_t conta[26]) {
    for (size_t k = 0; k < 26; k++) conta[k] = 0;
    for (; *s != '\0'; s++) {
        unsigned char c = (unsigned char)*s;   /* ctype vuole unsigned char */
        if (isalpha(c)) conta[tolower(c) - 'a']++;
    }
}

static void prova(const char *originale) {
    char s[256];
    snprintf(s, sizeof s, "%s", originale);    /* copia in un array scrivibile */

    printf("\"%s\": palindroma? %s\n", s, is_palindrome(s) ? "si'" : "no");

    size_t conta[26];
    freq(s, conta);
    printf("  lettere:");
    for (size_t k = 0; k < 26; k++)
        if (conta[k] > 0) printf(" %c=%zu", (char)('a' + k), conta[k]);
    printf("\n");

    reverse(s);
    printf("  rovesciata: \"%s\"\n", s);
}

int main(void) {
    /* attese: radar si', ciao no, "Anna" si' solo ignorando la maiuscola (qui no) */
    const char *fisse[] = { "radar", "ciao", "Anna", "a", "" };
    for (size_t i = 0; i < sizeof fisse / sizeof fisse[0]; i++) prova(fisse[i]);

    printf("\nScrivi una stringa per riga (Ctrl-D per finire):\n");
    char riga[256];
    while (fgets(riga, sizeof riga, stdin) != NULL) {
        riga[strcspn(riga, "\n")] = '\0';      /* via il fine riga di fgets */
        prova(riga);
    }
    return 0;
}
