/*
 * Esercizio 8.6 - I byte di un intero, due volte
 *
 * Idea: scomponi usa shift e maschere, (v >> (8 * i)) & 0xFF e' l'i-esimo byte dal
 * meno significativo, e il risultato non dipende dalla macchina: e' aritmetica, non
 * memoria. scomponi_union scrive il valore in una union e legge gli stessi quattro
 * byte attraverso il campo uint8_t[4]: byte[0] e' il byte all'indirizzo piu' basso,
 * e quale sia lo decide la macchina.
 * Su x86 e ARM (little-endian) i due risultati coincidono, BE BA FE CA, perche' il
 * byte meno significativo sta all'indirizzo piu' basso. Su una macchina big-endian
 * (SPARC, PowerPC in modalita' big, i vecchi 68000, e l'ordine di rete) la union
 * darebbe CA FE BA BE mentre gli shift darebbero ancora BE BA FE CA: il programma
 * stampa quale dei due casi e' quello della macchina su cui gira.
 * Errore evitato: una maschera 0xFF senza il cast a uint8_t funziona lo stesso, ma il
 * cast dice al lettore (e a -Wconversion, se acceso) che il troncamento e' voluto.
 */
#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

void scomponi(uint32_t v, uint8_t byte[4]) {
    for (int i = 0; i < 4; i++)
        byte[i] = (uint8_t)((v >> (8 * i)) & 0xFF);
}

void scomponi_union(uint32_t v, uint8_t byte[4]) {
    union {
        uint32_t valore;
        uint8_t byte[4];
    } d;
    d.valore = v;                             /* scrivo un campo... */
    for (int i = 0; i < 4; i++)
        byte[i] = d.byte[i];                  /* ...e leggo l'altro: gli stessi byte */
}

static void stampa_byte(const char *nome, const uint8_t byte[4]) {
    printf("%-16s", nome);
    for (int i = 0; i < 4; i++) printf(" %02X", byte[i]);
    printf("\n");
}

int main(void) {
    uint32_t v = 0xCAFEBABE;
    uint8_t con_shift[4], con_union[4];

    scomponi(v, con_shift);
    scomponi_union(v, con_union);

    printf("v = 0x%08" PRIX32 " (%" PRIu32 ")\n", v, v);
    stampa_byte("shift e maschere", con_shift);
    stampa_byte("union", con_union);

    if (memcmp(con_shift, con_union, 4) == 0)
        printf("coincidono: questa macchina e' little-endian, il byte meno "
               "significativo (BE) sta all'indirizzo piu' basso\n");
    else
        printf("NON coincidono: questa macchina e' big-endian, la union legge i byte "
               "nell'ordine in cui li si scrive\n");
    printf("Java (DataOutputStream.writeInt) e la rete mandano sempre CA FE BA BE: "
           "big-endian.\n");
    return 0;
}
