/*
 * Esercizio 4.9 - Il vettore di byte
 *
 * VettoreB tiene gli elementi stessi, uno dopo l'altro in un blocco solo: alla
 * creazione si dichiara quanti byte occupa un elemento (dim), inserire e'
 * copiare dim byte dentro il blocco (memcpy), leggere e' copiarli fuori. Lo
 * stesso programma del problema precedente (parole lette, ordinate, quelle
 * corte tolte), con elementi char[32].
 *
 * Confronto con il Vettore di void * (sol. 4.8), sullo stesso programma:
 * - allocazioni: qui una sola (piu' le realloc quando cresce); la' una per
 *   parola piu' l'array dei puntatori. Con 1000 parole: ~10 contro ~1010.
 * - chi libera: qui nessuna domanda, il vettore possiede una copia di ogni
 *   elemento e vb_free libera il blocco; la' serve la funzione `libera`.
 * - spazio: qui 32 byte per parola anche se e' "uva"; la' la lunghezza giusta
 *   piu' 8 byte di puntatore (e il costo di ogni malloc).
 * - indirizzi: qui vb_indirizzo(vb, i) vale solo fino al prossimo inserimento
 *   (realloc puo' spostare il blocco) o alla prossima rimozione (gli elementi
 *   scorrono); la' l'indirizzo di una parola resta valido finche' la parola
 *   e' nel vettore.
 * - limite: qui una parola piu' lunga di 31 caratteri viene troncata.
 *
 * Errore tipico evitato: riusare il comparatore della 4.8. La' gli elementi
 * erano char * e il comparatore riceveva char **; qui gli elementi sono
 * char[32] e il comparatore riceve gia' l'indirizzo delle lettere: strcmp va
 * chiamata direttamente sui due argomenti, senza togliere un livello.
 */
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char *dati;          /* n elementi consecutivi, dim byte ciascuno */
    size_t n, cap, dim;
} VettoreB;

VettoreB *vb_new(size_t dim) {
    VettoreB *vb = malloc(sizeof *vb);
    if (vb == NULL) return NULL;
    *vb = (VettoreB){ .dati = NULL, .n = 0, .cap = 0, .dim = dim };
    return vb;
}

static bool vb_cresci(VettoreB *vb) {
    size_t cap = vb->cap == 0 ? 4 : vb->cap * 2;
    char *dati = realloc(vb->dati, cap * vb->dim);
    if (dati == NULL) return false;
    vb->dati = dati;
    vb->cap = cap;
    return true;
}

bool vb_aggiungi(VettoreB *vb, const void *elemento) {
    if (vb->n == vb->cap && !vb_cresci(vb)) return false;
    memcpy(vb->dati + vb->n * vb->dim, elemento, vb->dim);
    vb->n++;
    return true;
}

size_t vb_quanti(const VettoreB *vb) { return vb->n; }

/* l'indirizzo dell'elemento i DENTRO il blocco: vale fino alla prossima modifica */
void *vb_indirizzo(const VettoreB *vb, size_t i) {
    return i < vb->n ? vb->dati + i * vb->dim : NULL;
}

bool vb_prendi(const VettoreB *vb, size_t i, void *elemento) {
    if (i >= vb->n) return false;
    memcpy(elemento, vb->dati + i * vb->dim, vb->dim);
    return true;
}

bool vb_rimuovi(VettoreB *vb, size_t i) {
    if (i >= vb->n) return false;
    char *qui = vb->dati + i * vb->dim;
    memmove(qui, qui + vb->dim, (vb->n - i - 1) * vb->dim);
    vb->n--;
    return true;
}

void vb_ordina(VettoreB *vb, int (*cmp)(const void *, const void *)) {
    qsort(vb->dati, vb->n, vb->dim, cmp);
}

void vb_free(VettoreB *vb) {
    if (vb == NULL) return;
    free(vb->dati);
    free(vb);
}

#define LUNGA 32

/* gli elementi sono char[LUNGA]: arriva l'indirizzo delle lettere */
static int cmp_parola(const void *a, const void *b) {
    return strcmp(a, b);
}

int main(void) {
    VettoreB *parole = vb_new(LUNGA);
    if (parole == NULL) return 1;

    char buf[LUNGA] = "";
    while (scanf("%31s", buf) == 1) {
        if (!vb_aggiungi(parole, buf)) break;       /* copia LUNGA byte di buf */
        memset(buf, 0, sizeof buf);
    }

    vb_ordina(parole, cmp_parola);
    for (size_t i = 0; i < vb_quanti(parole); ) {
        const char *p = vb_indirizzo(parole, i);
        if (strlen(p) < 4) vb_rimuovi(parole, i);
        else i++;
    }

    for (size_t i = 0; i < vb_quanti(parole); i++) {
        char parola[LUNGA];
        vb_prendi(parole, i, parola);
        printf("%s ", parola);
    }
    printf("\n");
    vb_free(parole);
    return 0;
}
