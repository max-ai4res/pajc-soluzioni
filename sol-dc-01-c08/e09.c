/*
 * Esercizio 8.9 - dump di una struct
 *
 * Idea: ogni struct viene riempita di 0xAA con memset, poi si scrivono i campi; i byte
 * che restano AA sono quelli che nessun campo tocca, cioe' il padding (e, nei campi
 * char[], la coda dopo il terminatore). La macro CAMPO(tipo, campo) produce la tripla
 * { "campo", offsetof(tipo, campo), sizeof(((tipo *)0)->campo) } senza scrivere numeri a
 * mano; dump_struct scorre la tabella dei campi, stampa una riga per campo con offset,
 * dimensione e byte, e in mezzo una riga "(padding)" per ogni buco fra la fine di un
 * campo e l'inizio del successivo, piu' la coda fino a sizeof. La dump del capitolo
 * sulle funzioni stampa la riga grezza per confronto.
 *
 * Dove sta il padding e come toglierlo:
 *  - Contatto (129 byte): nessun padding. Tutti i campi sono char[] o uint8_t, che si
 *    allineano a 1; per questo sizeof puo' essere dispari. Dentro nome e via si vedono
 *    gli AA dopo lo zero finale: non e' padding, e' spazio del campo non usato.
 *  - struct disordinata { char c; int32_t i; char d; } = 12: c a 0, tre AA perche' i
 *    cominci a 4 (multiplo di 4), d a 8, tre AA di coda perche' la struct sia un
 *    multiplo di 4 (in un array ogni i deve restare allineato). Riordinando
 *    { int32_t i; char c; char d; } viene 8: i a 0, c a 4, d a 5, due di coda.
 *  - struct conpad { char a; double b; char c; } = 24: a a 0, sette AA perche' b
 *    cominci a 8, c a 16, sette AA di coda per arrivare a un multiplo di 8.
 *    Riordinando { double b; char a; char c; } viene 16: b a 0, a a 8, c a 9, sei di
 *    coda; sotto 16 non si scende perche' il double impone l'allineamento a 8.
 * Regola: campi dal piu' grande al piu' piccolo.
 * Errore evitato: sommare i sizeof dei campi per sapere quanto occupa la struct (6 e
 * 10 invece di 12 e 24): a malloc e memcpy si da' sempre sizeof della struct.
 */
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

typedef struct {
    char via[48];
    char citta[32];
} Indirizzo;

typedef struct {
    char nome[32];
    char telefono[16];
    Indirizzo indirizzo;
    uint8_t flags;
} Contatto;

struct disordinata { char c; int32_t i; char d; };     /* 12 */
struct ordinata    { int32_t i; char c; char d; };     /* 8 */
struct conpad      { char a; double b; char c; };      /* 24 */
struct conpad_ord  { double b; char a; char c; };      /* 16 */

/* la dump del capitolo sulle funzioni: una riga, tutti i byte */
void dump(const char *nome, const void *p, size_t n) {
    const uint8_t *b = p;
    printf("%-8s %p  ", nome, p);
    for (size_t i = 0; i < n; i++)
        printf("%02X ", b[i]);
    printf("\n");
}

typedef struct {
    const char *nome;
    size_t offset, size;
} Campo;

#define CAMPO(tipo, campo) { #campo, offsetof(tipo, campo), sizeof(((tipo *)0)->campo) }

static void riga(const char *nome, size_t offset, size_t n, const uint8_t *b) {
    printf("  %-16s +%-4zu %3zu byte  ", nome, offset, n);
    for (size_t i = 0; i < n; i++) {
        if (i > 0 && i % 16 == 0) printf("\n%*s", 34, "");   /* a capo ogni 16 */
        printf("%02X ", b[offset + i]);
    }
    printf("\n");
}

static void dump_struct(const char *tipo, const void *base, size_t size,
                        const Campo *campi, size_t ncampi) {
    const uint8_t *b = base;
    size_t pos = 0;
    printf("%s: sizeof = %zu\n", tipo, size);
    for (size_t k = 0; k < ncampi; k++) {
        if (campi[k].offset > pos)
            riga("(padding)", pos, campi[k].offset - pos, b);
        riga(campi[k].nome, campi[k].offset, campi[k].size, b);
        pos = campi[k].offset + campi[k].size;
    }
    if (pos < size)
        riga("(padding)", pos, size - pos, b);
    printf("\n");
}

int main(void) {
    /* il contatto: 129 byte, nessun padding */
    Contatto c;
    memset(&c, 0xAA, sizeof c);
    snprintf(c.nome, sizeof c.nome, "%s", "Greta");
    snprintf(c.telefono, sizeof c.telefono, "%s", "030 1234567");
    snprintf(c.indirizzo.via, sizeof c.indirizzo.via, "%s", "Via Branze 38");
    snprintf(c.indirizzo.citta, sizeof c.indirizzo.citta, "%s", "Brescia");
    c.flags = 0x05;
    const Campo campi_contatto[] = {
        CAMPO(Contatto, nome),
        CAMPO(Contatto, telefono),
        CAMPO(Contatto, indirizzo.via),
        CAMPO(Contatto, indirizzo.citta),
        CAMPO(Contatto, flags),
    };
    dump_struct("Contatto", &c, sizeof c, campi_contatto,
                sizeof campi_contatto / sizeof campi_contatto[0]);

    /* la struct con l'int in mezzo: 12 byte, sei di padding */
    struct disordinata d;
    memset(&d, 0xAA, sizeof d);
    d.c = 1;
    d.i = 2;
    d.d = 3;
    dump("grezza", &d, sizeof d);
    const Campo campi_dis[] = {
        CAMPO(struct disordinata, c),
        CAMPO(struct disordinata, i),
        CAMPO(struct disordinata, d),
    };
    dump_struct("struct disordinata", &d, sizeof d, campi_dis,
                sizeof campi_dis / sizeof campi_dis[0]);

    struct ordinata o;
    memset(&o, 0xAA, sizeof o);
    o.i = 2;
    o.c = 1;
    o.d = 3;
    const Campo campi_ord[] = {
        CAMPO(struct ordinata, i),
        CAMPO(struct ordinata, c),
        CAMPO(struct ordinata, d),
    };
    dump_struct("struct ordinata (riordinata)", &o, sizeof o, campi_ord,
                sizeof campi_ord / sizeof campi_ord[0]);

    /* la struct con il double in mezzo: 24 byte, quattordici di padding */
    struct conpad p;
    memset(&p, 0xAA, sizeof p);
    p.a = 1;
    p.b = 2.0;
    p.c = 3;
    dump("grezza", &p, sizeof p);
    const Campo campi_pad[] = {
        CAMPO(struct conpad, a),
        CAMPO(struct conpad, b),
        CAMPO(struct conpad, c),
    };
    dump_struct("struct conpad", &p, sizeof p, campi_pad,
                sizeof campi_pad / sizeof campi_pad[0]);

    struct conpad_ord q;
    memset(&q, 0xAA, sizeof q);
    q.b = 2.0;
    q.a = 1;
    q.c = 3;
    const Campo campi_pad_ord[] = {
        CAMPO(struct conpad_ord, b),
        CAMPO(struct conpad_ord, a),
        CAMPO(struct conpad_ord, c),
    };
    dump_struct("struct conpad_ord (riordinata)", &q, sizeof q, campi_pad_ord,
                sizeof campi_pad_ord / sizeof campi_pad_ord[0]);

    printf("somma dei campi / sizeof: disordinata 6/%zu, ordinata 6/%zu, conpad 10/%zu, "
           "conpad_ord 10/%zu\n",
           sizeof d, sizeof o, sizeof p, sizeof q);
    return 0;
}
