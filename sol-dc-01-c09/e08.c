/*
 * Esercizio 9.8 - debug, release, asan
 *
 * Tre file: e08.c (il main), e08_calc.h / e08_calc.c (il modulo), piu' e08_util.h che
 * include e08_calc.h a sua volta: e08.c lo riceve quindi due volte, e senza la guardia
 * E08_CALC_H il typedef Statistiche verrebbe ridefinito ("typedef redefinition").
 *
 * Il Makefile richiesto dall'esercizio, scritto a mano (qui e' integrato in quello del
 * capitolo con i bersagli debug-e08 e release-e08; asan e clean sono quelli generali):
 *
 *     CC     = gcc
 *     CFLAGS = -std=c17 -Wall -Wextra -Werror -g
 *     SRC    = main.c calc.c
 *
 *     bin/calc: $(SRC) calc.h util.h | bin
 *     	$(CC) $(CFLAGS) -o $@ $(SRC)
 *     bin:
 *     	mkdir -p bin
 *     debug: CFLAGS += -DDEBUG -O0
 *     debug: bin/calc
 *     release: CFLAGS += -O2 -DNDEBUG
 *     release: bin/calc
 *     asan: CFLAGS += -fsanitize=address
 *     asan: bin/calc
 *     clean:
 *     	rm -rf bin
 *     .PHONY: debug release asan clean
 *
 * Che cosa cambia nei tre eseguibili (lo stesso sorgente, macro diverse):
 *   debug   (-DDEBUG -O0): LOG stampa su stderr file e riga di ogni passo; assert e'
 *           attivo e ./bin/e08 vuoto si ferma con "Assertion failed"; nessuna
 *           ottimizzazione, cosi' nel debugger ogni variabile e' dove il sorgente dice.
 *   release (-O2 -DNDEBUG): LOG diventa ((void)0) e non lascia traccia; assert sparisce
 *           (NDEBUG) e ./bin/e08 vuoto NON si ferma: divide zero per zero e stampa nan,
 *           perche' il controllo non c'e' piu'; il codice e' ottimizzato.
 *   asan    (-fsanitize=address): come il default (LOG spento, assert attivo) piu' i
 *           controlli sulla memoria a tempo di esecuzione; piu' lento e piu' grande.
 *
 * Perche' dopo make debug un semplice make non ricompila: make decide dalle date dei
 * file, non dalle opzioni. bin/e08 e' piu' recente di e08.c, e08_calc.c e degli header,
 * quindi per make e' aggiornato, anche se e' stato prodotto con -DDEBUG; le CFLAGS non
 * sono fra le dipendenze. Per questo i bersagli debug-e08 e release-e08 ricompilano
 * sempre (sono .PHONY e non dipendono da bin/e08), e per tornare al default si fa
 * make clean && make, oppure si tengono cartelle di uscita diverse per configurazione.
 */
#include <inttypes.h>
#include <stdio.h>
#include <string.h>

#include "e08_calc.h"
#include "e08_util.h"     /* include di nuovo e08_calc.h: senza guardia, errore */

int main(int argc, char **argv) {
    printf("build: %s\n", BUILD_NOME);
    LOG("inizio");

    int32_t voti[] = { 30, 27, 24, 18, 29 };
    size_t n = ARRAY_LEN(voti);

    printf("somma: attesa 128, ottenuta %" PRId64 "\n", calc_somma(voti, n));
    printf("media: attesa 25.6, ottenuta %g\n", calc_media(voti, n));
    Statistiche s = calc_statistiche(voti, n);
    printf("min %" PRId32 " (atteso 18), max %" PRId32 " (atteso 30), media %g\n",
           s.min, s.max, s.media);

    if (argc > 1 && strcmp(argv[1], "vuoto") == 0) {
        printf("media di un vettore vuoto: con assert attivo il programma si ferma qui\n");
        printf("media: %g (solo in release, dove il controllo e' sparito)\n", calc_media(voti, 0));
    }

    LOG("fine");
    return 0;
}
