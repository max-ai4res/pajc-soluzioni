/*
 * Esercizio 9.6 - Le frazioni
 *
 * Il modulo frazione (e06_frazione.h, e06_frazione.c) e questo main, che lo
 * usa includendo solo l'header. Frazione e' un tipo PUBBLICO, piccolo, che si
 * passa per valore: niente malloc, niente new/free. Le frazioni escono dal
 * modulo sempre ridotte ai minimi termini e con denominatore positivo.
 *
 * Scelte: mcd e riduci sono static nel sorgente del modulo, e nell'header non
 * compaiono: fuori dal modulo non esistono (un altro file potrebbe avere una
 * sua mcd senza conflitto). frazione_new restituisce un bool perche' puo'
 * fallire (denominatore zero) e consegna il risultato attraverso un
 * puntatore; le operazioni, che non falliscono, restituiscono per valore.
 *
 * Errore tipico evitato: fidarsi degli argomenti. Con il tipo pubblico chi usa
 * il modulo puo' scrivere (Frazione){ 2, -4 } senza passare da frazione_new:
 * il modulo non puo' impedirlo. Per questo ogni operazione riduce il proprio
 * risultato, e il confronto riduce gli argomenti prima di moltiplicare in
 * croce (un denominatore negativo invertirebbe il verso). Resta un limite
 * dichiarato: un denominatore zero scritto a mano e' un errore di chi usa il
 * modulo. Il problema 9.7 mostra l'altra strada, il tipo opaco.
 */
#include <stdio.h>

#include "e06_frazione.h"

static void riga(const char *testo, Frazione f) {
    printf("%s", testo);
    frazione_stampa(f);
    printf("\n");
}

int main(void) {
    Frazione a, b, c;
    if (!frazione_new(2, 4, &a) || !frazione_new(1, -3, &b)) return 1;
    riga("a = ", a);
    riga("b = ", b);
    riga("a + b = ", frazione_somma(a, b));
    riga("a * b = ", frazione_prodotto(a, b));
    printf("a confrontata con b: %d\n", frazione_confronta(a, b));
    printf("1/0: %s\n", frazione_new(1, 0, &c) ? "accettata" : "rifiutata");

    Frazione grezza = { 6, -8 };                /* costruita a mano, non ridotta */
    riga("6/-8 sommata a 0 = ", frazione_somma(grezza, (Frazione){ 0, 1 }));
    printf("6/-8 confrontata con -3/4: %d\n",
           frazione_confronta(grezza, (Frazione){ -3, 4 }));
    return 0;
}
