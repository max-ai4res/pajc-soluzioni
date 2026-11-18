/*
 * Esercizio 11.2 - La calcolatrice RPN a tabella
 *
 * Una calcolatrice in notazione polacca inversa: legge righe come
 *     3 4 + 2 *        -> 14
 *     9 sqrt neg       -> -3
 * Ogni parola e' un numero (va sulla pila) oppure un operatore, cercato in
 * OPERATORI: una riga per operatore, con il nome, quanti operandi toglie
 * dalla pila e la funzione che lo calcola. Il ciclo non nomina nessun
 * operatore: aggiungerne uno e' una funzione e una riga.
 *
 * Scelte: operatori binari e unari hanno firme diverse, quindi la riga
 * contiene DUE puntatori a funzione e il numero di operandi dice quale usare
 * (l'altro e' NULL). Una union sarebbe piu' compatta, ma renderebbe meno
 * leggibile l'inizializzazione della tabella. Gli errori (pila vuota, parola
 * sconosciuta, divisione per zero, valori rimasti sulla pila) sono un enum
 * con i messaggi in una tabella parallela generata da una X-macro.
 *
 * Errore tipico evitato: l'ordine degli operandi. "8 2 -" toglie prima il 2 e
 * poi l'8, e deve calcolare 8 - 2, non 2 - 8: il primo valore tolto dalla
 * pila e' il SECONDO operando.
 */
#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_PILA 64

#define ERRORI_X                                        \
    X(OK,          "")                                  \
    X(PILA_VUOTA,  "mancano operandi")                  \
    X(PILA_PIENA,  "troppi valori")                     \
    X(SCONOSCIUTO, "parola sconosciuta")                \
    X(DIV_ZERO,    "divisione per zero")                \
    X(AVANZI,      "sono rimasti valori sulla pila")

typedef enum {
#define X(codice, messaggio) codice,
    ERRORI_X
#undef X
} Esito;

static const char *const MESSAGGI[] = {
#define X(codice, messaggio) messaggio,
    ERRORI_X
#undef X
};

typedef double (*Binaria)(double, double);
typedef double (*Unaria)(double);

static double add(double a, double b)      { return a + b; }
static double sub(double a, double b)      { return a - b; }
static double mul(double a, double b)      { return a * b; }
static double divide(double a, double b)   { return a / b; }
static double potenza(double a, double b)  { return pow(a, b); }
static double neg(double a)                { return -a; }
static double radice(double a)             { return sqrt(a); }

typedef struct {
    const char *nome;
    int operandi;            /* 1 o 2 */
    Binaria binaria;         /* se operandi == 2 */
    Unaria unaria;           /* se operandi == 1 */
    bool divisore;           /* il secondo operando non puo' essere zero */
} Operatore;

static const Operatore OPERATORI[] = {
    { "+",    2, add,     NULL,   false },
    { "-",    2, sub,     NULL,   false },
    { "*",    2, mul,     NULL,   false },
    { "/",    2, divide,  NULL,   true  },
    { "^",    2, potenza, NULL,   false },
    { "neg",  1, NULL,    neg,    false },
    { "sqrt", 1, NULL,    radice, false },
};
#define N_OPERATORI (sizeof OPERATORI / sizeof OPERATORI[0])

static const Operatore *cerca(const char *nome) {
    for (size_t i = 0; i < N_OPERATORI; i++)
        if (strcmp(OPERATORI[i].nome, nome) == 0) return &OPERATORI[i];
    return NULL;
}

/* calcola l'espressione (che viene spezzata sul posto); il risultato in *r */
static Esito calcola(char *espressione, double *r) {
    double pila[MAX_PILA];
    size_t n = 0;

    for (char *p = strtok(espressione, " \t\n"); p != NULL; p = strtok(NULL, " \t\n")) {
        char *fine;
        double x = strtod(p, &fine);
        if (fine != p && *fine == '\0') {               /* un numero */
            if (n == MAX_PILA) return PILA_PIENA;
            pila[n++] = x;
            continue;
        }
        const Operatore *op = cerca(p);
        if (op == NULL) return SCONOSCIUTO;
        if (n < (size_t)op->operandi) return PILA_VUOTA;
        if (op->operandi == 2) {
            double b = pila[--n], a = pila[--n];        /* il primo tolto e' il SECONDO */
            if (op->divisore && b == 0) return DIV_ZERO;
            pila[n++] = op->binaria(a, b);
        } else {
            pila[n - 1] = op->unaria(pila[n - 1]);
        }
    }
    if (n == 0) return PILA_VUOTA;
    if (n > 1) return AVANZI;
    *r = pila[0];
    return OK;
}

int main(void) {
    char riga[256];
    while (fgets(riga, sizeof riga, stdin) != NULL) {
        if (riga[strspn(riga, " \t\n")] == '\0') continue;      /* riga vuota */
        double r;
        Esito e = calcola(riga, &r);
        if (e == OK) printf("%g\n", r);
        else printf("errore: %s\n", MESSAGGI[e]);
    }
    return 0;
}
