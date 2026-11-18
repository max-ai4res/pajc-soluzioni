/*
 * Esercizio 6.7 - Una stringa nuova
 *
 * concatena misura a e b con strlen, chiede la + lb + 1 byte (il +1 e' lo
 * zero finale: dimenticarlo e' l'overflow di un byte) e copia con memcpy,
 * la seconda volta la + 1 byte compreso il terminatore; duplica e' il caso
 * con una stringa sola (strdup, che non e' nel C standard). Entrambe
 * restituiscono un blocco sullo heap che il CHIAMANTE libera; se malloc
 * fallisce restituiscono NULL e il main lo controlla.
 *
 * Se concatena restituisse un array locale (char r[256]; ...; return r;),
 * restituirebbe l'indirizzo di una variabile del proprio frame, che non
 * esiste piu' al ritorno: un puntatore pendente che "sembra funzionare"
 * finche' la prossima chiamata non riscrive quella zona dello stack (clang
 * lo segnala con -Wreturn-stack-address, e -Werror lo blocca). Lo heap e'
 * l'unico posto per un valore che deve sopravvivere alla funzione.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* a seguita da b in una stringa nuova; il chiamante libera con free */
char *concatena(const char *a, const char *b) {
    size_t la = strlen(a), lb = strlen(b);
    char *r = malloc(la + lb + 1);           /* +1: lo zero finale */
    if (r == NULL) return NULL;
    memcpy(r, a, la);
    memcpy(r + la, b, lb + 1);               /* copia anche il '\0' di b */
    return r;
}

/* copia di s sullo heap; il chiamante libera con free */
char *duplica(const char *s) {
    size_t len = strlen(s);
    char *r = malloc(len + 1);
    if (r == NULL) return NULL;
    memcpy(r, s, len + 1);
    return r;
}

int main(void) {
    char *saluto = concatena("Ciao, ", "Greta");
    char *vuota = concatena("", "");
    char *copia = duplica("Loretta");
    if (saluto == NULL || vuota == NULL || copia == NULL) {
        fprintf(stderr, "memoria insufficiente\n");
        free(saluto);
        free(vuota);
        free(copia);
        return EXIT_FAILURE;
    }

    printf("concatena(\"Ciao, \", \"Greta\") = \"%s\" (attesa \"Ciao, Greta\", %zu caratteri)\n",
           saluto, strlen(saluto));
    printf("concatena(\"\", \"\") = \"%s\" (attesa vuota, %zu caratteri)\n",
           vuota, strlen(vuota));
    printf("duplica(\"Loretta\") = \"%s\"\n", copia);

    copia[0] = 'l';                          /* la copia e' nostra: si puo' scrivere */
    printf("la copia modificata: \"%s\"; l'originale e' un letterale e non si tocca\n", copia);

    char *tutto = concatena(saluto, copia);
    if (tutto != NULL) printf("concatena(saluto, copia) = \"%s\"\n", tutto);

    free(saluto);  saluto = NULL;
    free(vuota);   vuota = NULL;
    free(copia);   copia = NULL;
    free(tutto);   tutto = NULL;
    return 0;
}
