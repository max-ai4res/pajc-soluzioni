/*
 * Esercizio 4.8 - Esito e risultati
 *
 * bool dividi(int a, int b, int *q, int *r) restituisce l'esito e scrive i
 * risultati, quoziente e resto, attraverso i puntatori: false se b e' zero,
 * e in quel caso *q e *r restano com'erano. q e r possono essere NULL quando
 * al chiamante interessa un risultato solo: `if (q) *q = ...` davanti a ogni
 * scrittura.
 *
 * Errore tipico evitato: scrivere *q = a / b PRIMA di controllare b. Il
 * programma morirebbe per divisione per zero e il valore di ritorno non
 * servirebbe a nulla. Prima l'esito, poi i risultati.
 *
 * Il main mostra i tre modi di chiamarla (entrambi i risultati, solo il
 * quoziente, solo il resto) e poi legge coppie a b da stdin fino a EOF o a un
 * input non numerico, stampando la divisione o un messaggio quando non si puo'.
 * Prova: printf '17 5\n8 0\n-7 2\n' | ./bin/e08
 */
#include <stdio.h>
#include <stdbool.h>

bool dividi(int a, int b, int *q, int *r) {
    if (b == 0)
        return false;                          /* prima l'esito... */
    if (q)
        *q = a / b;                            /* ...poi i risultati */
    if (r)
        *r = a % b;
    return true;
}

static void stampa_divisione(int a, int b) {
    int q = 0, r = 0;
    if (dividi(a, b, &q, &r))
        printf("%d / %d = %d resto %d\n", a, b, q, r);
    else
        printf("%d / %d: non si puo' dividere per zero\n", a, b);
}

int main(void) {
    int q = -1, r = -1;

    printf("dividi(17, 5, &q, &r): esito %d, q = %d, r = %d (atteso 1, 3, 2)\n",
           dividi(17, 5, &q, &r), q, r);
    printf("dividi(17, 5, &q, NULL): esito %d, q = %d (atteso 1, 3)\n",
           dividi(17, 5, &q, NULL), q);
    printf("dividi(17, 5, NULL, &r): esito %d, r = %d (atteso 1, 2)\n",
           dividi(17, 5, NULL, &r), r);
    q = -1;
    r = -1;
    printf("dividi(17, 0, &q, &r): esito %d, q = %d, r = %d "
           "(atteso 0, -1, -1: non toccati)\n",
           dividi(17, 0, &q, &r), q, r);

    printf("\ncoppie `a b` da dividere (fine con EOF o una parola):\n");
    int a, b;
    while (scanf("%d %d", &a, &b) == 2)
        stampa_divisione(a, b);
    return 0;
}
