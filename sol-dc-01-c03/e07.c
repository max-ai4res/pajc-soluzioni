/*
 * Esercizio 3.7 - La misura del frame
 *
 * interna(const char *esterna) dichiara una locale e stampa la distanza in
 * byte fra il suo indirizzo e quello ricevuto dal main: e' la misura di quanto
 * stack separa i due frame (la differenza fra due char * e' un ptrdiff_t, %td).
 * interna_grande fa lo stesso con in piu' un array di cento int: la distanza
 * cresce di almeno 400 byte, perche' l'array sta nel frame.
 *
 * Con quella distanza si stima quante chiamate ricorsive di una funzione con
 * lo stesso frame entrano in 8 MB (lo stack del thread principale su macOS e
 * Linux): 8 MB / distanza. La verifica e' profonda(), ricorsiva con lo stesso
 * array di cento int, che stampa la profondita' raggiunta a intervalli
 * regolari prima di morire di stack overflow (segmentation fault). La dispensa
 * dice "ogni centomila chiamate", ma con un frame di qualche centinaio di byte
 * in 8 MB entrano circa ventimila chiamate: il passo e' quindi la stima diviso
 * dieci, cosi' si vedono una decina di righe. Con stdout in pipe o in un file
 * l'output e' bufferizzato e il crash lo perderebbe: fflush dopo ogni riga.
 *
 * La ricorsione senza fine e' dietro l'argomento `crash`:
 *     ./bin/e07          misura e stima, e termina normalmente
 *     ./bin/e07 crash    anche la verifica, che finisce con un segfault
 * cosi' il programma si puo' lanciare in una verifica automatica senza morire.
 *
 * Lo stack cresce verso gli indirizzi bassi, quindi esterna - &mia e' positivo;
 * i numeri cambiano con compilatore, opzioni (-O, -fsanitize) e architettura.
 * Sottrarre puntatori a oggetti diversi non e' C "pulito": qui serve solo a
 * misurare, non va fatto nel codice vero.
 */
#include <stdio.h>
#include <stddef.h>
#include <string.h>
#include <limits.h>

#define INT_NEL_FRAME 100
#define STACK_BYTE (8u * 1024u * 1024u)

static ptrdiff_t interna(const char *esterna) {
    char mia = 0;
    ptrdiff_t d = esterna - &mia;
    printf("interna:        locale a %p, distanza dal main %td byte\n",
           (void *)&mia, d);
    return d;
}

static ptrdiff_t interna_grande(const char *esterna) {
    int buf[INT_NEL_FRAME];
    char mia = 0;
    buf[0] = 1;                               /* l'array va usato, o sparisce */
    ptrdiff_t d = esterna - &mia;
    printf("interna_grande: locale a %p, distanza dal main %td byte "
           "(con %d int = %zu byte nel frame)\n",
           (void *)&mia, d, INT_NEL_FRAME, sizeof buf);
    return d + buf[0] - 1;
}

/* Ricorsione senza fine con lo stesso frame di interna_grande: stampa la
 * profondita' ogni `passo` chiamate e prima o poi muore di stack overflow.
 * La somma con buf[0] dopo la chiamata impedisce al compilatore di
 * trasformare la ricorsione in un ciclo (tail call). */
static unsigned long profonda(unsigned long livello, unsigned long passo) {
    int buf[INT_NEL_FRAME];
    if (livello == ULONG_MAX)          /* mai vero: lo stack finisce molto
                                          prima, ma senza un'uscita il
                                          compilatore rifiuta la ricorsione
                                          infinita (-Winfinite-recursion) */
        return 0;
    buf[0] = (int)(livello % INT_NEL_FRAME);
    if (livello % passo == 0) {
        printf("profondita' %lu\n", livello);
        fflush(stdout);
    }
    return profonda(livello + 1, passo) + (unsigned long)buf[0];
}

int main(int argc, char *argv[]) {
    char locale = 0;
    printf("main:           locale a %p\n", (void *)&locale);

    ptrdiff_t d1 = interna(&locale);
    ptrdiff_t d2 = interna_grande(&locale);
    printf("l'array di %d int ha allungato il frame di %td byte\n",
           INT_NEL_FRAME, d2 - d1);

    unsigned long stima = STACK_BYTE / (unsigned long)d2;
    printf("stima: %u byte / %td byte a chiamata = circa %lu chiamate "
           "ricorsive in 8 MB\n", STACK_BYTE, d2, stima);

    if (argc > 1 && strcmp(argv[1], "crash") == 0) {
        unsigned long passo = stima / 10 > 0 ? stima / 10 : 1;
        printf("verifica: ricorsione senza fine, una riga ogni %lu chiamate, "
               "fino al segmentation fault...\n", passo);
        fflush(stdout);
        profonda(0, passo);
        printf("non si arriva qui\n");
    } else {
        printf("(con l'argomento `crash` parte la verifica: il programma "
               "muore di stack overflow)\n");
    }
    return 0;
}
