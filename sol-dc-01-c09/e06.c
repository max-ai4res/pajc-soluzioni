/*
 * Esercizio 9.6 - Macro che mentono
 *
 * Il sorgente di partenza aveva tre macro sbagliate, che qui sono gia' corrette; il
 * main non e' stato toccato.
 *   #define SQUARE(x) x * x
 *     SQUARE(3 + 1) diventava 3 + 1 * 3 + 1 = 7, non 16: la macro incolla testo e non
 *     conosce la precedenza. Corretta: ((x) * (x)), ogni parametro fra parentesi e tutta
 *     la macro fra parentesi (10 / SQUARE(2) senza le esterne sarebbe 10 / 2 * 2 = 10).
 *   #define LOG(msg) fprintf(stderr, "[%s:%d] %s\n", __FILE__, __LINE__, msg)
 *     stampava sempre, anche nel programma finale. Corretta: dentro #ifdef DEBUG, e
 *     ((void)0) altrimenti, cosi' senza -DDEBUG sparisce e non costa nulla.
 *   #define SWAP(a, b) int tmp = a; a = b; b = tmp
 *     sotto un if senza graffe solo la prima istruzione era condizionata, le altre due
 *     venivano eseguite sempre; anzi, una dichiarazione sotto un if non e' nemmeno
 *     un'istruzione e il compilatore si fermava ("expected expression"). Corretta con
 *     do { int tmp_ = (a); (a) = (b); (b) = tmp_; } while (0): una sola istruzione che
 *     pretende il ; di chi la usa, e sta anche davanti a un else.
 * In piu' SWAP_T(T, a, b), con il tipo come parametro, per scambiare double e puntatori.
 * Verifica: gcc -E -P e06.c | grep -A3 'int main' mostra il testo che il compilatore
 * riceve; make debug ricompila bin/e06 con -DDEBUG e il LOG compare su stderr.
 */
#include <stdio.h>

#define SQUARE(x) ((x) * (x))

#ifdef DEBUG
#define LOG(msg) fprintf(stderr, "[%s:%d] %s\n", __FILE__, __LINE__, msg)
#else
#define LOG(msg) ((void)0)
#endif

#define SWAP(a, b) do { int tmp_ = (a); (a) = (b); (b) = tmp_; } while (0)

#define SWAP_T(T, a, b) do { T tmp_ = (a); (a) = (b); (b) = tmp_; } while (0)

int main(void) {
    printf("SQUARE(3 + 1):   atteso 16, ottenuto %d\n", SQUARE(3 + 1));
    printf("10 / SQUARE(2):  atteso 2,  ottenuto %d\n", 10 / SQUARE(2));

    int x = 7, y = 3;
    if (x > y) SWAP(x, y); else printf("nessuno scambio\n");
    printf("SWAP sotto l'if: attesi x = 3, y = 7; ottenuti x = %d, y = %d\n", x, y);
    if (x > y) SWAP(x, y); else printf("nessuno scambio: x = %d, y = %d restano cosi'\n", x, y);

    int somma = 0;
    for (int i = 0; i < 3; i++) {
        LOG("iterazione");                     /* solo con -DDEBUG (make debug) */
        somma += i;
    }
    printf("somma del ciclo: attesa 3, ottenuta %d (LOG: %s)\n", somma,
#ifdef DEBUG
           "acceso, tre righe su stderr qui sopra");
#else
           "spento, nessuna riga su stderr");
#endif

    double w = 3.5, h = 2.0;
    SWAP_T(double, w, h);
    printf("SWAP_T(double):  attesi w = 2, h = 3.5; ottenuti w = %g, h = %g\n", w, h);

    const char *p = "Greta", *q = "Max";
    SWAP_T(const char *, p, q);
    printf("SWAP_T(const char *): attesi p = Max, q = Greta; ottenuti p = %s, q = %s\n", p, q);
    return 0;
}
