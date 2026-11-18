/*
 * Esercizio 9.9 - La rubrica a moduli
 *
 * Una rubrica interattiva in tre moduli e questo main:
 *
 *                main (e09.c)
 *               /            \
 *        comandi  ---------->  rubrica      una freccia e' un #include:
 *               \            /              chi sta in alto conosce chi sta
 *                 contatto                  in basso, mai il contrario
 *
 *   contatto: tipo pubblico (nome e telefono in due array), per valore;
 *   rubrica:  tipo opaco, possiede una copia di ogni contatto, li tiene
 *             ordinati per nome, li cerca con bsearch, li percorre con una
 *             callback con contesto. Non stampa e non legge;
 *   comandi:  la tabella dei comandi e le loro funzioni, tutte static. E' il
 *             solo modulo che scrive sullo schermo. Pubblica una funzione.
 *   main:     crea la rubrica, legge le righe, le consegna, libera.
 *
 * Il grafo non ha cicli: contatto non include nessuno; rubrica include
 * contatto; comandi include rubrica e contatto; main include comandi e
 * rubrica. e09_comandi.h, che deve solo nominare il tipo Rubrica in un
 * prototipo, non include e09_rubrica.h: ridichiara il nome (dichiarazione
 * anticipata), e l'inclusione vera sta in e09_comandi.c.
 *
 * Scelte: gli header sono stati scritti per primi, con i contratti; i
 * sorgenti dopo. rubrica_aggiungi restituisce un enum, perche' i modi di
 * fallire sono due (nome gia' presente, memoria) e a decidere che cosa dire
 * all'utente e' il modulo comandi, non la rubrica.
 *
 * Errore tipico evitato: mettere una printf nella rubrica ("contatto
 * aggiunto"), o far leggere a comandi i campi della rubrica. Nel primo caso
 * la rubrica non si potrebbe usare in un programma senza console; nel secondo
 * il tipo opaco lo impedisce a compilazione.
 */
#include <stdio.h>

#include "e09_comandi.h"
#include "e09_rubrica.h"

int main(void) {
    Rubrica *r = rubrica_new();
    if (r == NULL) return 1;

    char riga[128];
    printf("rubrica (aiuto per i comandi)\n");
    while (fgets(riga, sizeof riga, stdin) != NULL)
        if (!comandi_esegui(r, riga)) break;

    rubrica_free(r);
    return 0;
}
