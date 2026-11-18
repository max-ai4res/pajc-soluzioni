/* Esercizio 10.4 - Il decodificatore di byte
 * Il programma riceve un uint32_t in esadecimale dalla riga di comando (strtoul con
 * base 16, con il controllo che il testo sia tutto cifre e che il valore stia in 32
 * bit) e lo guarda in quattro modi: i byte come stanno in memoria (union con un array
 * di uint8_t: su x86 e ARM little-endian, il meno significativo per primo), i byte in
 * ordine big-endian (shift e maschere, indipendenti dalla macchina), il valore con i
 * byte rovesciati (htonl scritto a mano), e i quattro campi di un header di 32 bit
 * (versione 4 bit, tipo 4, flag 8, lunghezza 16, dal bit piu' alto), estratti con le
 * maschere e poi con una struct a campi di bit dichiarati nello stesso ordine.
 * Scelte: nel rovesciamento ogni byte viene convertito a uint32_t PRIMA dello shift,
 * perche' uint8_t promuove a int con segno e (b << 24) con b >= 128 finirebbe nel bit
 * del segno (comportamento indefinito). Il confronto fra maschere e campi di bit dice
 * come il compilatore ha disposto i campi: se i due risultati coincidono li ha messi
 * dall'alto (il primo campo nei bit alti), altrimenti dal basso; su gcc e clang per
 * x86 e ARM e' dal basso, ed e' il motivo per cui un formato esterno si legge con le
 * maschere, mai con i campi di bit. Infine sizeof delle due struct.
 * Prova: ./bin/e04 CAFEBABE   oppure   ./bin/e04 0x1A2B3C4D
 */
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <inttypes.h>
#include <errno.h>

/* I campi dell'header nell'ordine della specifica, dal bit piu' alto. */
struct header_bit {
    uint32_t versione  : 4;
    uint32_t tipo      : 4;
    uint32_t flag      : 8;
    uint32_t lunghezza : 16;
};

/* Gli stessi quattro campi come interi normali. */
struct header_int {
    unsigned versione, tipo, flag, lunghezza;
};

/* htonl a mano: il byte piu' alto diventa il piu' basso e viceversa. */
static uint32_t rovescia(uint32_t v) {
    uint8_t b3 = (uint8_t)(v >> 24);
    uint8_t b2 = (uint8_t)(v >> 16);
    uint8_t b1 = (uint8_t)(v >> 8);
    uint8_t b0 = (uint8_t)v;
    return ((uint32_t)b0 << 24) | ((uint32_t)b1 << 16) | ((uint32_t)b2 << 8) | (uint32_t)b3;
}

static int leggi_hex(const char *s, uint32_t *out) {
    char *fine;
    errno = 0;
    unsigned long v = strtoul(s, &fine, 16);
    if (fine == s || *fine != '\0' || errno == ERANGE || v > UINT32_MAX) return 0;
    *out = (uint32_t)v;
    return 1;
}

int main(int argc, char *argv[]) {
    uint32_t v;
    if (argc != 2 || !leggi_hex(argv[1], &v)) {
        fprintf(stderr, "uso: %s valore_esadecimale   (es. %s CAFEBABE)\n", argv[0], argv[0]);
        return 1;
    }
    printf("valore: 0x%08" PRIX32 " = %" PRIu32 "\n", v, v);

    /* 1. i byte come stanno in memoria: la union li legge uno a uno */
    union {
        uint32_t valore;
        uint8_t byte[4];
    } u = { .valore = v };
    printf("in memoria (indirizzi crescenti): ");
    for (int i = 0; i < 4; i++) printf("%02X ", u.byte[i]);
    printf("  -> macchina %s-endian\n", u.byte[0] == (v & 0xFF) ? "little" : "big");

    /* 2. big-endian con shift e maschere: non dipende dalla macchina */
    printf("big-endian (shift e maschere):    ");
    for (int i = 3; i >= 0; i--) printf("%02X ", (unsigned)((v >> (8 * i)) & 0xFF));
    printf("\n");

    /* 3. i byte rovesciati */
    uint32_t r = rovescia(v);
    printf("rovesciato (htonl a mano): 0x%08" PRIX32 ", rovesciato due volte: 0x%08" PRIX32 " %s\n",
           r, rovescia(r), rovescia(r) == v ? "ok" : "DIVERSO");

    /* 4. i campi dell'header con le maschere */
    unsigned versione  = (v >> 28) & 0xF;
    unsigned tipo      = (v >> 24) & 0xF;
    unsigned flag      = (v >> 16) & 0xFF;
    unsigned lunghezza = v & 0xFFFF;
    printf("header con le maschere:  versione %u  tipo %u  flag 0x%02X  lunghezza %u\n",
           versione, tipo, flag, lunghezza);

    /* 5. gli stessi campi con la struct a campi di bit sovrapposta ai 32 bit */
    union {
        uint32_t valore;
        struct header_bit h;
    } hb = { .valore = v };
    printf("header con i campi di bit: versione %u  tipo %u  flag 0x%02X  lunghezza %u\n",
           (unsigned)hb.h.versione, (unsigned)hb.h.tipo, (unsigned)hb.h.flag,
           (unsigned)hb.h.lunghezza);
    int uguali = hb.h.versione == versione && hb.h.tipo == tipo &&
                 hb.h.flag == flag && hb.h.lunghezza == lunghezza;
    if (uguali)
        printf("i due risultati coincidono: il compilatore ha messo i campi dall'ALTO\n"
               "(il primo campo dichiarato nei bit piu' alti)\n");
    else
        printf("i due risultati NON coincidono: il compilatore ha messo i campi dal BASSO\n"
               "(il primo campo dichiarato nei bit piu' bassi): per un formato esterno\n"
               "valgono le maschere, non i campi di bit\n");

    /* 6. sizeof */
    printf("sizeof(struct header_bit) = %zu, sizeof(struct header_int) = %zu\n",
           sizeof(struct header_bit), sizeof(struct header_int));
    return 0;
}
