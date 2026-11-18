/*
 * Esercizio 2.6 - La calcolatrice a tabella
 *
 * ./bin/e06 3 + 4   stampa 7. Le operazioni stanno in OPERAZIONI: una riga per
 * simbolo, con la funzione che lo calcola (un BinaryOp). calcola() percorre la
 * tabella e chiama cio' che trova; il messaggio per il simbolo sconosciuto
 * elenca i simboli leggendoli dalla tabella stessa, cosi' non puo' restare
 * indietro. La moltiplicazione e' 'x' perche' la shell espanderebbe '*'.
 *
 * Scelte: gli operandi si leggono con strtol e si controlla che la stringa sia
 * stata consumata tutta ("3a" non e' un numero). La divisione per zero si
 * controlla PRIMA di chiamare, con un campo della riga che dice se
 * l'operazione vuole il secondo operando diverso da zero: la firma di
 * BinaryOp resta la stessa per tutte.
 *
 * Errore tipico evitato: far restituire a divide() un valore "speciale" (0, o
 * -1) in caso di divisione per zero. Ogni int e' un risultato legittimo: 0/5
 * fa 0, e chi chiama non potrebbe distinguere.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

typedef int (*BinaryOp)(int, int);

static int add(int a, int b)       { return a + b; }
static int subtract(int a, int b)  { return a - b; }
static int multiply(int a, int b)  { return a * b; }
static int divide(int a, int b)    { return a / b; }
static int remainder_(int a, int b) { return a % b; }

typedef struct {
    char simbolo;
    BinaryOp calcola;
    bool divisore;          /* il secondo operando non puo' essere zero */
} Operazione;

static const Operazione OPERAZIONI[] = {
    { '+', add,        false },
    { '-', subtract,   false },
    { 'x', multiply,   false },
    { '/', divide,     true  },
    { '%', remainder_, true  },
};
#define N_OPERAZIONI (sizeof OPERAZIONI / sizeof OPERAZIONI[0])

static const Operazione *cerca(char simbolo) {
    for (size_t i = 0; i < N_OPERAZIONI; i++)
        if (OPERAZIONI[i].simbolo == simbolo) return &OPERAZIONI[i];
    return NULL;
}

static bool leggi_int(const char *s, int *x) {
    char *fine;
    long v = strtol(s, &fine, 10);
    if (fine == s || *fine != '\0') return false;
    *x = (int)v;
    return true;
}

int main(int argc, char *argv[]) {
    int a, b;
    if (argc != 4 || !leggi_int(argv[1], &a) || !leggi_int(argv[3], &b)
            || argv[2][0] == '\0' || argv[2][1] != '\0') {
        fprintf(stderr, "uso: %s numero simbolo numero\n", argv[0]);
        return 1;
    }

    const Operazione *op = cerca(argv[2][0]);
    if (op == NULL) {
        fprintf(stderr, "operazione sconosciuta: %s; disponibili:", argv[2]);
        for (size_t i = 0; i < N_OPERAZIONI; i++)
            fprintf(stderr, " %c", OPERAZIONI[i].simbolo);
        fprintf(stderr, "\n");
        return 1;
    }
    if (op->divisore && b == 0) {
        fprintf(stderr, "divisione per zero\n");
        return 1;
    }

    printf("%d\n", op->calcola(a, b));
    return 0;
}
