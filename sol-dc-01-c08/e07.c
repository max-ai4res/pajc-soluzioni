/*
 * Esercizio 8.7 - Tre flag in un byte
 *
 * Idea: ogni flag e' un bit di un uint8_t, e la sua maschera e' 1u << k. | con la
 * maschera accende, & ~maschera spegne, & legge. stampa_binario scorre i bit dal
 * settimo allo zero (da sinistra a destra, come si scrive un numero) con (f >> i) & 1,
 * e conta_accesi fa lo stesso ciclo sommando gli 1.
 * Errore evitato: ~m con m uint8_t e' un int a 32 bit con i bit alti a uno
 * (promozione intera): assegnarlo con &= a un uint8_t va bene perche' si tengono i
 * bassi, ma il cast (uint8_t)~m dice che cosa si vuole e non lascia dubbi. Allo stesso
 * modo un uint8_t passato a printf viene promosso a int: si stampa con %u dopo un cast
 * a unsigned, cosi' il formato e il tipo combaciano.
 */
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

enum { FLAG_ERRORE = 1u << 0, FLAG_URGENTE = 1u << 1, FLAG_LETTO = 1u << 2 };

void flag_set(uint8_t *f, uint8_t m)   { *f |= m; }
void flag_clear(uint8_t *f, uint8_t m) { *f &= (uint8_t)~m; }
bool flag_test(uint8_t f, uint8_t m)   { return (f & m) != 0; }

void stampa_binario(uint8_t f) {
    for (int i = 7; i >= 0; i--)
        putchar(((f >> i) & 1) ? '1' : '0');
}

int conta_accesi(uint8_t f) {
    int n = 0;
    for (int i = 0; i < 8; i++)
        if ((f >> i) & 1) n++;
    return n;
}

static void mostra(const char *operazione, uint8_t f) {
    printf("%-34s ", operazione);
    stampa_binario(f);
    printf("  (%3u)  accesi: %d\n", (unsigned)f, conta_accesi(f));
}

int main(void) {
    uint8_t f = 0;
    mostra("inizio", f);

    flag_set(&f, FLAG_URGENTE | FLAG_LETTO);
    mostra("set URGENTE | LETTO", f);                 /* 00000110, 6 */
    flag_clear(&f, FLAG_URGENTE);
    mostra("clear URGENTE", f);                       /* 00000100, 4 */
    flag_set(&f, FLAG_ERRORE);
    mostra("set ERRORE", f);                          /* 00000101, 5 */
    flag_set(&f, FLAG_ERRORE);
    mostra("set ERRORE di nuovo (idempotente)", f);   /* 00000101, 5 */
    flag_clear(&f, FLAG_LETTO | FLAG_URGENTE);
    mostra("clear LETTO | URGENTE", f);               /* 00000001, 1 */
    flag_set(&f, FLAG_ERRORE | FLAG_URGENTE | FLAG_LETTO);
    mostra("set tutti e tre", f);                     /* 00000111, 7 */

    printf("\nflag_test su %u:\n", (unsigned)f);
    printf("  ERRORE  acceso? atteso si, ottenuto %s\n", flag_test(f, FLAG_ERRORE) ? "si" : "no");
    printf("  URGENTE acceso? atteso si, ottenuto %s\n", flag_test(f, FLAG_URGENTE) ? "si" : "no");
    flag_clear(&f, FLAG_URGENTE);
    printf("  dopo clear URGENTE: acceso? atteso no, ottenuto %s\n",
           flag_test(f, FLAG_URGENTE) ? "si" : "no");
    printf("  bit 7 (nessun flag):  atteso no, ottenuto %s\n",
           flag_test(f, 1u << 7) ? "si" : "no");

    printf("\n");
    mostra("stato finale", f);                        /* 00000101, 2 accesi */
    return 0;
}
