/*
 * Esercizio 3.6 - dump di tutto
 *
 * Usa la dump della dispensa (nome, indirizzo, byte in esadecimale) su
 * valori di tipo diverso e spiega, accanto a ogni chiamata, i byte stampati.
 * Tutte le spiegazioni valgono per una macchina little-endian (x86-64 e
 * ARM64 di Mac e PC): il byte MENO significativo sta all'indirizzo piu'
 * basso, cioe' e' il primo che dump stampa.
 *
 * Scelte: la char[8] e' inizializzata con "ciao", cosi' i tre byte dopo lo
 * zero terminatore sono zero anche loro (un inizializzatore riempie di zeri
 * cio' che non specifica); una seconda char[8] senza inizializzatore mostra
 * la differenza: i suoi byte sono quello che c'era sullo stack.
 * (long long) 1 << 40: il letterale 1 e' un int a 32 bit e 1 << 40 sarebbe
 * un overflow; il cast va fatto PRIMA dello shift.
 */
#include <stdio.h>
#include <stdint.h>

/* dump: nome, indirizzo e gli n byte a partire da p */
void dump(const char *nome, const void *p, size_t n) {
    const uint8_t *b = p;              /* la stessa memoria, un byte alla volta */
    printf("%-8s %p  ", nome, p);
    for (size_t i = 0; i < n; i++)
        printf("%02X ", b[i]);
    printf("\n");
}

int main(void) {
    int n = 258;
    /* 258 = 0x0102: byte 02 01 00 00. Il 258 sta nei primi due byte, il meno
     * significativo (02) per primo perche' la macchina e' little-endian. */
    dump("int", &n, sizeof n);

    short s = -1;
    /* FF FF: in complemento a due -1 ha tutti i bit a 1, su qualunque
     * larghezza; uno short e' 2 byte. */
    dump("short", &s, sizeof s);

    double d = 1.0;
    /* 00 00 00 00 00 00 F0 3F: IEEE 754, 1.0 = 0x3FF0000000000000.
     * Segno 0, esponente 0x3FF (= 1023, cioe' 2^0), mantissa tutta zero
     * (l'1 iniziale e' implicito). Letto dall'ultimo byte: 3F F0 00 ... */
    dump("1.0", &d, sizeof d);

    d = 2.0;
    /* 00 00 00 00 00 00 00 40: 2.0 = 0x4000000000000000. Cambia solo
     * l'esponente, 0x400 = 1024 (2^1); la mantissa resta zero. Nessun byte
     * "raddoppia": in virgola mobile raddoppiare e' sommare 1 all'esponente. */
    dump("2.0", &d, sizeof d);

    char ciao[8] = "ciao";
    /* 63 69 61 6F 00 00 00 00: i codici ASCII di c i a o, lo zero
     * terminatore, poi tre zeri: l'inizializzatore riempie di zeri il resto. */
    dump("ciao", ciao, sizeof ciao);

    char vuota[8];
    /* Senza inizializzatore: 8 byte di spazzatura (quello che c'era in quella
     * zona dello stack), diversi da un'esecuzione all'altra e da compilatore a
     * compilatore. Leggerli e' permesso qui solo come byte, tramite dump. */
    dump("vuota", vuota, sizeof vuota);

    long long grande = (long long)1 << 40;
    /* 00 00 00 00 00 01 00 00: 2^40 = 0x0000010000000000, un solo bit a 1,
     * il bit 40, che sta nel sesto byte (byte 5, bit 0: 5*8 = 40). */
    dump("2^40", &grande, sizeof grande);

    return 0;
}
