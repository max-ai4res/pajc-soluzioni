/*
 * Esercizio 3.8 - Per valore e per puntatore, misurati
 *
 * Grande e' una struct di 1000 double (sizeof = 8000 byte). somma_v la riceve
 * per valore, somma_p per puntatore a const; entrambe sommano gli elementi.
 * Il main le chiama un milione di volte ciascuna e misura con clock() di
 * time.h (tempo di CPU del processo, in CLOCKS_PER_SEC per secondo).
 *
 * Differenza osservata: somma_v e' piu' lenta, perche' a ogni chiamata il
 * chiamante copia 8000 byte nel frame della chiamata (in C si passa sempre per
 * valore: una struct si copia tutta), poi la funzione somma; somma_p copia solo
 * 8 byte di indirizzo e somma leggendo l'originale. La somma di mille double
 * costa uguale nelle due versioni, quindi la differenza e' tutta la copia: un
 * milione di volte 8 KB, otto gigabyte spostati in memoria. Quanto pesi in
 * percentuale dipende da compilatore e opzioni (a -O0 la somma e' lenta e la
 * copia e' una frazione; a -O2 la somma si vettorizza e la copia domina).
 *
 * Con una struct di due double (16 byte) non si osserverebbe nulla: copiare 16
 * byte costa come copiare un puntatore (l'ABI li passa addirittura in due
 * registri, senza toccare la memoria), e la versione per puntatore paga in piu'
 * una lettura indiretta. Il passaggio per puntatore conviene per le struct
 * grandi, o quando la funzione deve modificare l'originale; per le piccole il
 * valore e' altrettanto veloce e piu' sicuro (nessun alias).
 *
 * I tempi assoluti variano da macchina a macchina e da esecuzione a esecuzione.
 */
#include <stdio.h>
#include <time.h>

#define N 1000
#define RIPETIZIONI 1000000

typedef struct { double v[N]; } Grande;

static double somma_v(Grande g) {
    double s = 0.0;
    for (size_t i = 0; i < N; i++)
        s += g.v[i];
    return s;
}

static double somma_p(const Grande *g) {
    double s = 0.0;
    for (size_t i = 0; i < N; i++)
        s += g->v[i];
    return s;
}

int main(void) {
    Grande g;
    for (size_t i = 0; i < N; i++)
        g.v[i] = (double)i;                   /* somma attesa: 0+1+...+999 */
    double attesa = (double)(N - 1) * N / 2.0;

    printf("sizeof(Grande) = %zu byte, %d chiamate per versione\n",
           sizeof(Grande), RIPETIZIONI);

    double tot_v = 0.0;
    clock_t t0 = clock();
    for (int k = 0; k < RIPETIZIONI; k++)
        tot_v += somma_v(g);
    clock_t t1 = clock();

    double tot_p = 0.0;
    for (int k = 0; k < RIPETIZIONI; k++)
        tot_p += somma_p(&g);
    clock_t t2 = clock();

    double sec_v = (double)(t1 - t0) / CLOCKS_PER_SEC;
    double sec_p = (double)(t2 - t1) / CLOCKS_PER_SEC;
    printf("per valore    (somma_v): %.3f s\n", sec_v);
    printf("per puntatore (somma_p): %.3f s\n", sec_p);
    if (sec_p > 0.0)
        printf("rapporto valore/puntatore: %.2f\n", sec_v / sec_p);

    /* controllo che le due versioni abbiano calcolato la stessa cosa */
    printf("somma: attesa %.0f, somma_v %.0f, somma_p %.0f\n",
           attesa, tot_v / RIPETIZIONI, tot_p / RIPETIZIONI);
    return 0;
}
