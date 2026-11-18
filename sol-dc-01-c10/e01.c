/* Esercizio 10.1 - Prevedere prima di eseguire
 * Dieci frammenti sull'aritmetica dei puntatori e la precedenza, il passaggio per
 * valore e per indirizzo, sizeof dentro e fuori da una funzione, i letterali e gli
 * interi senza segno. Per ciascuno il programma stampa il valore previsto su carta e
 * quello calcolato, e dice se coincidono.
 * Scelte. Il frammento (2) e' comportamento indefinito: (*q)++ + *q modifica e legge
 * v[0] nella stessa espressione senza un punto di sequenza; clang con -Wall lo rifiuta
 * ("unsequenced modification and access", errore con -Werror) e comunque un numero
 * "giusto" uscito per caso non dimostrerebbe nulla. Non viene eseguito: il programma
 * stampa il perche'. Nel frammento (1), dopo *p++ il puntatore sta una cella OLTRE la
 * fine dell'array: calcolarlo e confrontarlo e' lecito, leggerlo no; si stampa quindi
 * la posizione p - i, non *p. Nel frammento (7) il compilatore rifiuta da solo sizeof
 * su un parametro array (-Wsizeof-array-argument): quell'avviso e' gia' la risposta, e
 * qui viene spento per la sola funzione h per poter stampare il numero.
 */
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stddef.h>

static void f(int n) {          /* (3): n e' una copia, il chiamante non vede nulla */
    n = 9;
    (void)n;
}

static void g(int *n) {         /* (4): scrive nella variabile del chiamante */
    *n = 9;
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wsizeof-array-argument"
static size_t h(double d[]) {   /* (7): d e' un double *, sizeof misura il puntatore */
    return sizeof d;
}
#pragma GCC diagnostic pop

static void confronta(int num, const char *cosa, long long previsto, long long ottenuto) {
    printf("(%2d) %-34s previsto %4lld  ottenuto %4lld  %s\n", num, cosa, previsto,
           ottenuto, previsto == ottenuto ? "ok" : "DIVERSO");
}

int main(void) {
    /* (1) */
    {
        int i[] = {3, 1, 2};
        int *p = &i[*i - 1];        /* *i e' 3: p punta a i[2] */
        int k = *p++;               /* *(p++): vale i[2], poi p avanza oltre la fine */
        confronta(1, "k = *p++ con p = &i[2]", 2, k);
        confronta(1, "p - i dopo (una cella oltre)", 3, (long long)(p - i));
        printf("     *p non si legge: p == i + 3 e' un indirizzo valido solo per i confronti\n");
    }
    /* (2) */
    printf("( 2) a = (*q)++ + *q                 NON ESEGUITO: comportamento indefinito\n"
           "     v[0] viene modificato e letto senza un punto di sequenza fra le due\n"
           "     operazioni; -Wall lo segnala (unsequenced modification and access)\n");
    /* (3) e (4) */
    {
        int x = 5;
        f(x);
        confronta(3, "x dopo f(x) con n = 9", 5, x);
        g(&x);
        confronta(4, "x dopo g(&x) con *n = 9", 9, x);
    }
    /* (5) */
    {
        char s[6] = "ciao";
        s[2] = 0;                   /* lo zero in mezzo: la stringa finisce li' */
        confronta(5, "strlen(s) con s[2] = 0", 2, (long long)strlen(s));
        confronta(5, "sizeof s", 6, (long long)sizeof s);
    }
    /* (6) */
    {
        int m[2][3] = {{1, 2, 3}, {4, 5, 6}};
        int *p = &m[0][0];          /* la matrice e' un blocco: p[4] e' il quinto int */
        confronta(6, "p[4] con p = &m[0][0]", 5, p[4]);
    }
    /* (7) */
    {
        double d[4];
        confronta(7, "sizeof d nel main", 32, (long long)sizeof d);
        confronta(7, "sizeof d dentro h(double d[])", 8, (long long)h(d));
    }
    /* (8) */
    {
        const char *t = "abc";
        t++;                        /* il puntatore avanza, il letterale no */
        confronta(8, "*t dopo t++ ('b' = 98)", 'b', *t);
        printf("     t[0] = 'x' e' vietato: t e' const char * (errore di compilazione),\n"
               "     e il letterale sta in memoria di sola lettura (crash senza il const)\n");
    }
    /* (9) */
    {
        int x = 1;
        int *p = &x;
        int **pp = &p;
        **pp += *p;                 /* x += x */
        confronta(9, "x dopo **pp += *p", 2, x);
    }
    /* (10) */
    {
        uint8_t b = 250;
        b += 10;                    /* 260 non ci sta: gira modulo 256 */
        confronta(10, "uint8_t b = 250; b += 10", 4, b);
    }
    return 0;
}
