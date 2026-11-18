/*
 * Esercizio 8.8 - Contare i riferimenti
 *
 * Un piccolo sistema a conteggio dei riferimenti: rc_new alloca un blocco con
 * un contatore a 1 e una funzione da chiamare quando il blocco muore;
 * rc_retain incrementa; rc_release decrementa e, a zero, chiama la funzione e
 * libera. E' un modo di gestire la memoria alternativo sia al proprietario
 * unico del C sia al garbage collector di Java: ogni oggetto sa quanti lo
 * stanno usando.
 *
 * L'idea: contatore e funzione stanno in un'intestazione DAVANTI ai dati,
 * come l'intestazione di un oggetto Java. rc_new restituisce l'indirizzo dei
 * dati (cosi' chi usa l'oggetto non vede l'intestazione), e le altre funzioni
 * tornano indietro di una misura fissa per ritrovarla.
 *
 * Il programma fa tre prove, e conta gli oggetti vivi:
 *  1. una catena a -> b: rilasciato a, muore a e, attraverso la sua funzione
 *     distruggi, viene rilasciato b, che muore anche lui. Zero vivi.
 *  2. un ciclo a <-> b: rilasciati entrambi dal main, ciascuno resta con il
 *     contatore a 1 per via dell'altro. Due oggetti vivi che nessuno puo'
 *     piu' raggiungere: un leak (lo vedono `leaks --atExit -- ./bin/e08` su
 *     macOS e AddressSanitizer su Linux). E' cio' che il GC di Java, che
 *     guarda la raggiungibilita' dalle radici, non soffre.
 *  3. il rimedio: uno dei due riferimenti e' "debole", cioe' non conta (b
 *     indica a senza retain). Il ciclo non tiene piu' in vita nessuno. In
 *     cambio il riferimento debole e' un prestito: vale finche' a e' vivo.
 *
 * Errore tipico evitato: una distruggi che non rilascia i riferimenti
 * dell'oggetto che muore. Anche senza cicli, gli oggetti indicati da un
 * oggetto morto resterebbero con il conto troppo alto, per sempre.
 */
#include <stdalign.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

typedef void (*Distruggi)(void *dati);

typedef struct {
    size_t conto;
    Distruggi distruggi;     /* chiamata prima di liberare; puo' essere NULL */
} Intestazione;

static size_t vivi = 0;      /* per la dimostrazione: quanti blocchi esistono */

/* la misura dell'intestazione, arrotondata perche' i dati partano allineati */
static size_t testa(void) {
    size_t passo = alignof(max_align_t);
    return (sizeof(Intestazione) + passo - 1) / passo * passo;
}

static Intestazione *intestazione(void *dati) {
    return (Intestazione *)((char *)dati - testa());
}

/* un blocco di dim byte azzerati, con il conto a 1. Va rilasciato con rc_release. */
void *rc_new(size_t dim, Distruggi distruggi) {
    Intestazione *h = calloc(1, testa() + dim);
    if (h == NULL) return NULL;
    h->conto = 1;
    h->distruggi = distruggi;
    vivi++;
    return (char *)h + testa();
}

/* un riferimento in piu': chi lo prende dovra' rilasciarlo */
void *rc_retain(void *dati) {
    if (dati != NULL) intestazione(dati)->conto++;
    return dati;
}

/* un riferimento in meno; a zero l'oggetto muore */
void rc_release(void *dati) {
    if (dati == NULL) return;
    Intestazione *h = intestazione(dati);
    if (--h->conto > 0) return;
    if (h->distruggi != NULL) h->distruggi(dati);
    free(h);
    vivi--;
}

size_t rc_conto(void *dati) { return intestazione(dati)->conto; }

/* ---- un nodo che si riferisce a un altro ---- */
typedef struct Nodo Nodo;
struct Nodo {
    const char *nome;
    Nodo *altro;             /* riferimento che conta (retain), oppure... */
    bool debole;             /* ...debole: non conta, e non va rilasciato */
};

static void nodo_distruggi(void *dati) {
    Nodo *n = dati;
    printf("    muore %s\n", n->nome);
    if (!n->debole) rc_release(n->altro);       /* rilascia cio' a cui si riferiva */
}

static Nodo *nodo_new(const char *nome) {
    Nodo *n = rc_new(sizeof *n, nodo_distruggi);
    if (n != NULL) n->nome = nome;
    return n;
}

/* n si riferisce a verso, contandolo */
static void collega(Nodo *n, Nodo *verso) {
    n->altro = rc_retain(verso);
    n->debole = false;
}

/* n si riferisce a verso senza contarlo: un prestito */
static void collega_debole(Nodo *n, Nodo *verso) {
    n->altro = verso;
    n->debole = true;
}

int main(void) {
    printf("1. catena a -> b\n");
    Nodo *a = nodo_new("a"), *b = nodo_new("b");
    if (a == NULL || b == NULL) return 1;
    collega(a, b);
    printf("    conti: a %zu, b %zu\n", rc_conto(a), rc_conto(b));
    rc_release(b);                              /* il main non usa piu' b: resta ad a */
    rc_release(a);                              /* muore a, e con lui b */
    printf("    oggetti vivi: %zu\n", vivi);

    printf("2. ciclo a <-> b\n");
    a = nodo_new("a");
    b = nodo_new("b");
    if (a == NULL || b == NULL) return 1;
    collega(a, b);
    collega(b, a);
    printf("    conti: a %zu, b %zu\n", rc_conto(a), rc_conto(b));
    rc_release(a);
    rc_release(b);                              /* nessuno muore: restano a 1 */
    printf("    oggetti vivi: %zu  <- il leak del ciclo\n", vivi);
    size_t persi = vivi;

    printf("3. ciclo con un riferimento debole\n");
    a = nodo_new("a");
    b = nodo_new("b");
    if (a == NULL || b == NULL) return 1;
    collega(a, b);
    collega_debole(b, a);                       /* b guarda a, ma non lo tiene in vita */
    printf("    conti: a %zu, b %zu\n", rc_conto(a), rc_conto(b));
    rc_release(b);
    rc_release(a);
    printf("    oggetti vivi oltre ai %zu persi prima: %zu\n", persi, vivi - persi);
    return 0;
}
