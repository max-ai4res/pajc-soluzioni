/*
 * Esercizio 11.1 - Leggere e scrivere dichiarazioni
 *
 * Dodici dichiarazioni lette a parole (nel commento accanto a ciascuna) e sei
 * scritte a partire dalla descrizione. Il file compila: e' il compilatore a
 * verificare le risposte, perche' a ogni variabile dichiarata viene assegnato
 * qualcosa del tipo descritto, e un'assegnazione fra tipi diversi sarebbe un
 * errore (con -Werror anche un avviso lo e').
 *
 * Il metodo: si parte dal nome; si va a destra finche' si puo' (tonde:
 * "funzione che riceve...", quadre: "array di..."); poi a sinistra
 * (asterisco: "puntatore a..."); usciti da una coppia di parentesi si
 * ricomincia. Per scrivere si fa il percorso al contrario, oppure, meglio, si
 * da' un nome al tipo intermedio con typedef (sezione finale).
 *
 * Errore tipico evitato: leggere da sinistra a destra come una frase. In
 * `int *f(int)` l'asterisco non appartiene a f: f e' una funzione, e
 * l'asterisco al suo risultato. Sono le parentesi attorno a (*g) a fare di g
 * un puntatore.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* ---- funzioni qualunque, per avere indirizzi da assegnare ---- */
static int quadrato(int x)                { return x * x; }
static int somma(int a, int b)            { return a + b; }
static double meta(double x)              { return x / 2; }
static void saluta(void)                  { }
static int *primo(int *v)                 { return v; }
static char *copia(const char *s)         { (void)s; return NULL; }
static int confronta(const void *a, const void *b) { (void)a; (void)b; return 0; }
static void gestore(int segnale)          { (void)segnale; }

/* ---- 12 dichiarazioni da leggere ---- */

/* 1. a e' un puntatore a int */
static int *a;
/* 2. b e' un array di 3 puntatori a int */
static int *b[3];
/* 3. c e' un puntatore a un array di 3 int */
static int (*c)[3];
/* 4. d e' una FUNZIONE che riceve un int e restituisce un puntatore a int (prototipo) */
static int *d(int n);
/* 5. e e' un PUNTATORE a una funzione che riceve un int e restituisce un int */
static int (*e)(int);
/* 6. f e' un puntatore a una funzione che riceve due int e restituisce un int */
static int (*f)(int, int);
/* 7. g e' un array di 4 puntatori a funzioni da double a double */
static double (*g[4])(double);
/* 8. h e' un puntatore a una funzione senza parametri che non restituisce nulla */
static void (*h)(void);
/* 9. i e' un puntatore a una funzione che riceve un puntatore a int e
      restituisce un puntatore a int */
static int *(*i)(int *);
/* 10. j e' un puntatore a una funzione che riceve un const char * e
       restituisce un char * */
static char *(*j)(const char *);
/* 11. k e' un puntatore a una funzione che riceve due const void * e
       restituisce un int: il comparatore di qsort */
static int (*k)(const void *, const void *);
/* 12. scegli e' una FUNZIONE che riceve un int e restituisce un puntatore a
       una funzione che riceve un int e non restituisce nulla (e' la forma di
       signal() della libreria) */
static void (*scegli(int quale))(int);

static int *d(int n) { static int valore; valore = n; return &valore; }
static void (*scegli(int quale))(int) { (void)quale; return gestore; }

/* ---- 6 dichiarazioni da scrivere ---- */

/* A. un puntatore a una funzione che riceve un double e restituisce un double */
static double (*pA)(double);
/* B. un array di 2 puntatori a funzioni senza parametri e senza risultato */
static void (*pB[2])(void);
/* C. una funzione che riceve un puntatore a funzione (int -> int) e un int, e
      restituisce un int */
static int applica(int (*fn)(int), int x) { return fn(x); }
/* D. un puntatore a una funzione come la C */
static int (*pD)(int (*)(int), int);
/* E. una funzione che riceve un char e restituisce un puntatore a una funzione
      (int, int) -> int: la "tabella" della calcolatrice, come funzione */
static int (*operazione(char simbolo))(int, int) { (void)simbolo; return somma; }
/* F. un puntatore a un array di 5 puntatori a char */
static char *(*pF)[5];

/* ---- le stesse, con i typedef: cosi' si scrivono davvero ---- */
typedef int (*Unaria)(int);
typedef int (*Binaria)(int, int);
typedef void (*Gestore)(int);

static int applica2(Unaria fn, int x) { return fn(x); }         /* come la C */
static Binaria operazione2(char simbolo) { return operazione(simbolo); }  /* come la E */
static Gestore scegli2(int quale) { return scegli(quale); }     /* come la 12 */

int main(void) {
    static int numero = 7, terna[3] = { 1, 2, 3 };
    static char *parole[5];

    /* se una riga non compila, la lettura corrispondente e' sbagliata */
    a = &numero;
    b[0] = &numero;
    c = &terna;
    a = d(5);
    e = quadrato;
    f = somma;
    g[0] = meta;
    h = saluta;
    i = primo;
    j = copia;
    k = confronta;
    Gestore q = scegli(1);

    pA = meta;
    pB[0] = saluta;
    pD = applica;
    Binaria op = operazione('+');
    pF = &parole;

    h();
    q(0);
    printf("e(4) = %d, f(2, 3) = %d, g[0](9) = %g\n", e(4), f(2, 3), g[0](9));
    printf("*a = %d, (*c)[2] = %d, *i(terna) = %d\n", *a, (*c)[2], *i(terna));
    printf("pD(quadrato, 5) = %d, op(4, 5) = %d\n", pD(quadrato, 5), op(4, 5));
    printf("con i typedef: %d %d\n", applica2(quadrato, 6), operazione2('+')(1, 2));
    (void)j; (void)k; (void)pA; (void)pB; (void)pF; (void)scegli2; (void)b;
    return 0;
}
