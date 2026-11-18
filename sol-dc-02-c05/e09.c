/*
 * Esercizio 5.9 - La matrice, in due modi
 *
 * Matrix: dimensioni e un blocco unico di int per righe. Le quattro
 * operazioni elemento per elemento (+, -, *, /) sono scritte due volte:
 *  - generate dalla macro MTX_EL_BY_EL_OP, che riceve l'OPERATORE come quarto
 *    argomento (cosa che una funzione non puo' ricevere);
 *  - con mtx_el_by_el, che riceve un BinaryOp, cioe' una funzione.
 * Tutte controllano che le tre matrici abbiano le stesse dimensioni e
 * restituiscono false se no. Il programma di prova e' scritto una volta:
 * riceve le quattro funzioni di una famiglia in una tabella e confronta ogni
 * risultato con quello atteso.
 *
 * Scelte: la macro contiene il controllo delle dimensioni e un `return
 * false`, quindi va usata solo dentro funzioni che restituiscono bool: e' un
 * vincolo che una funzione non avrebbe, ed e' scritto nel commento. I nomi
 * locali della macro finiscono con _ per non confondersi con quelli di chi la
 * usa.
 *
 * Errore tipico evitato: MTX_LEN senza le parentesi esterne, che funziona in
 * `MTX_LEN(m) + 3` e sbaglia in `x / MTX_LEN(m)`. E la divisione per zero:
 * la macro con `/` non puo' controllare il divisore senza imporre il
 * controllo anche agli altri operatori, quindi mtx_div_m divide e basta (qui
 * i dati di prova non hanno zeri); la versione con la funzione usa una
 * dividi() che lo zero lo gestisce. E' il punto in cui la funzione e' piu'
 * flessibile della macro.
 */
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct { size_t rows, cols; int *data; } Matrix;

#define MTX_LEN(m) ((m)->rows * (m)->cols)

Matrix *mtx_new(size_t rows, size_t cols) {
    Matrix *m = malloc(sizeof *m);
    if (m == NULL) return NULL;
    m->rows = rows;
    m->cols = cols;
    m->data = calloc(rows * cols, sizeof *m->data);
    if (m->data == NULL) { free(m); return NULL; }
    return m;
}

void mtx_free(Matrix *m) {
    if (m == NULL) return;
    free(m->data);
    free(m);
}

/* servono esattamente rows * cols argomenti int: nessuno lo controlla */
void mtx_set_values(Matrix *m, ...) {
    va_list ap;
    va_start(ap, m);
    for (size_t i = 0; i < MTX_LEN(m); i++) m->data[i] = va_arg(ap, int);
    va_end(ap);
}

void mtx_print(const Matrix *m) {
    for (size_t i = 0; i < m->rows; i++) {
        for (size_t j = 0; j < m->cols; j++) printf("%5d", m->data[i * m->cols + j]);
        printf("\n");
    }
}

static bool stesse_dimensioni(const Matrix *a, const Matrix *b, const Matrix *c) {
    return a->rows == b->rows && a->cols == b->cols
        && a->rows == c->rows && a->cols == c->cols;
}

/* ---- prima famiglia: la macro con l'operatore come parametro ----
   da usare solo dentro una funzione che restituisce bool */
#define MTX_EL_BY_EL_OP(a, b, c, op)                                    \
    do {                                                                \
        if (!stesse_dimensioni((a), (b), (c))) return false;            \
        const int *pa_ = (a)->data, *pb_ = (b)->data;                   \
        int *pc_ = (c)->data;                                           \
        for (size_t i_ = 0; i_ < MTX_LEN(a); i_++)                      \
            pc_[i_] = pa_[i_] op pb_[i_];                               \
    } while (0)

bool mtx_add_m(const Matrix *a, const Matrix *b, Matrix *c) { MTX_EL_BY_EL_OP(a, b, c, +); return true; }
bool mtx_sub_m(const Matrix *a, const Matrix *b, Matrix *c) { MTX_EL_BY_EL_OP(a, b, c, -); return true; }
bool mtx_mul_m(const Matrix *a, const Matrix *b, Matrix *c) { MTX_EL_BY_EL_OP(a, b, c, *); return true; }
bool mtx_div_m(const Matrix *a, const Matrix *b, Matrix *c) { MTX_EL_BY_EL_OP(a, b, c, /); return true; }

/* ---- seconda famiglia: il puntatore a funzione ---- */
typedef int (*BinaryOp)(int, int);

static int add(int x, int y)      { return x + y; }
static int subtract(int x, int y) { return x - y; }
static int multiply(int x, int y) { return x * y; }
static int dividi(int x, int y)   { return y == 0 ? 0 : x / y; }   /* lo zero e' gestito */

bool mtx_el_by_el(const Matrix *a, const Matrix *b, Matrix *c, BinaryOp f) {
    if (!stesse_dimensioni(a, b, c)) return false;
    for (size_t i = 0; i < MTX_LEN(a); i++) c->data[i] = f(a->data[i], b->data[i]);
    return true;
}

bool mtx_add_f(const Matrix *a, const Matrix *b, Matrix *c) { return mtx_el_by_el(a, b, c, add); }
bool mtx_sub_f(const Matrix *a, const Matrix *b, Matrix *c) { return mtx_el_by_el(a, b, c, subtract); }
bool mtx_mul_f(const Matrix *a, const Matrix *b, Matrix *c) { return mtx_el_by_el(a, b, c, multiply); }
bool mtx_div_f(const Matrix *a, const Matrix *b, Matrix *c) { return mtx_el_by_el(a, b, c, dividi); }

/* ---- la prova, scritta una volta per le due famiglie ---- */
typedef bool (*Operazione)(const Matrix *, const Matrix *, Matrix *);
typedef struct { const char *nome; Operazione op; int attesi[6]; } Prova;

static int prova(const char *famiglia, const Operazione ops[4]) {
    const Prova PROVE[4] = {
        { "somma",      ops[0], { 11, 22, 33, 44, 55, 66 } },
        { "differenza", ops[1], { 9, 18, 27, 36, 45, 54 } },
        { "prodotto",   ops[2], { 10, 40, 90, 160, 250, 360 } },
        { "divisione",  ops[3], { 10, 10, 10, 10, 10, 10 } },
    };
    Matrix *a = mtx_new(2, 3), *b = mtx_new(2, 3), *c = mtx_new(2, 3), *d = mtx_new(3, 2);
    if (a == NULL || b == NULL || c == NULL || d == NULL) {
        mtx_free(a); mtx_free(b); mtx_free(c); mtx_free(d);
        return 1;
    }
    mtx_set_values(a, 10, 20, 30, 40, 50, 60);
    mtx_set_values(b, 1, 2, 3, 4, 5, 6);

    int errori = 0;
    for (size_t i = 0; i < 4; i++) {
        bool ok = PROVE[i].op(a, b, c)
               && memcmp(c->data, PROVE[i].attesi, sizeof PROVE[i].attesi) == 0
               && !PROVE[i].op(a, b, d);            /* dimensioni diverse: false */
        printf("%-9s %-10s %s\n", famiglia, PROVE[i].nome, ok ? "ok" : "SBAGLIATA");
        if (!ok) errori++;
    }
    mtx_free(a); mtx_free(b); mtx_free(c); mtx_free(d);
    return errori;
}

int main(void) {
    const Operazione MACRO[4]    = { mtx_add_m, mtx_sub_m, mtx_mul_m, mtx_div_m };
    const Operazione FUNZIONE[4] = { mtx_add_f, mtx_sub_f, mtx_mul_f, mtx_div_f };
    int errori = prova("macro", MACRO) + prova("funzione", FUNZIONE);

    Matrix *m = mtx_new(2, 2);
    if (m == NULL) return 1;
    mtx_set_values(m, 1, 2, 3, 4);
    mtx_print(m);
    mtx_free(m);
    return errori != 0;
}
