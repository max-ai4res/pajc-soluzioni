/* frazione: numeri razionali, sempre ridotti e con denominatore positivo */
#include "e06_frazione.h"

#include <stdio.h>

/* le due funzioni di servizio: static, fuori da questo file non esistono */
static long mcd(long a, long b) {
    if (a < 0) a = -a;
    if (b < 0) b = -b;
    while (b != 0) {
        long r = a % b;
        a = b;
        b = r;
    }
    return a;
}

/* minimi termini e denominatore positivo; den non deve essere 0 */
static Frazione riduci(long num, long den) {
    long d = mcd(num, den);
    if (den < 0) d = -d;
    return (Frazione){ num / d, den / d };
}

bool frazione_new(long num, long den, Frazione *f) {
    if (den == 0) return false;
    *f = riduci(num, den);
    return true;
}

/* gli argomenti potrebbero non essere ridotti (il tipo e' pubblico): il risultato
   passa comunque da riduci */
Frazione frazione_somma(Frazione a, Frazione b) {
    return riduci(a.num * b.den + b.num * a.den, a.den * b.den);
}

Frazione frazione_prodotto(Frazione a, Frazione b) {
    return riduci(a.num * b.num, a.den * b.den);
}

int frazione_confronta(Frazione a, Frazione b) {
    a = riduci(a.num, a.den);                   /* denominatori positivi: il verso */
    b = riduci(b.num, b.den);                   /* del confronto e' sicuro          */
    long x = a.num * b.den, y = b.num * a.den;
    return (x > y) - (x < y);
}

void frazione_stampa(Frazione f) {
    if (f.den == 1) printf("%ld", f.num);
    else printf("%ld/%ld", f.num, f.den);
}
