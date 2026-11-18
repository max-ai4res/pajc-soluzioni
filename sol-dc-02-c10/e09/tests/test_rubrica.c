/* prove del modulo rubrica */
#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#include "contatto.h"
#include "rubrica.h"

/* raccoglie i nomi nell'ordine in cui arrivano: "Greta Max Nami " */
static void raccogli(const Contatto *c, void *ctx) {
    char *testo = ctx;
    strcat(testo, c->nome);
    strcat(testo, " ");
}

int main(void) {
    Rubrica *r = rubrica_new();
    assert(r != NULL);
    assert(rubrica_quanti(r) == 0);
    assert(rubrica_cerca(r, "Greta") == NULL);

    EsitoRubrica e1 = rubrica_aggiungi(r, contatto_new("Nami", "333"));
    EsitoRubrica e2 = rubrica_aggiungi(r, contatto_new("Greta", "347"));
    EsitoRubrica e3 = rubrica_aggiungi(r, contatto_new("Max", "320"));
    EsitoRubrica e4 = rubrica_aggiungi(r, contatto_new("Nami", "000"));
    assert(e1 == RUBRICA_OK && e2 == RUBRICA_OK && e3 == RUBRICA_OK);
    assert(e4 == RUBRICA_DOPPIO);                   /* il nome c'e' gia' */
    assert(rubrica_quanti(r) == 3);

    const Contatto *c = rubrica_cerca(r, "Nami");
    assert(c != NULL && strcmp(c->telefono, "333") == 0);   /* il primo, non il doppio */

    char testo[64] = "";
    rubrica_per_ognuno(r, raccogli, testo);
    assert(strcmp(testo, "Greta Max Nami ") == 0);  /* in ordine di nome */

    bool tolto = rubrica_togli(r, "Max");           /* cio' che agisce, fuori dall'assert */
    assert(tolto);
    assert(rubrica_quanti(r) == 2);
    assert(rubrica_cerca(r, "Max") == NULL);
    tolto = rubrica_togli(r, "Max");
    assert(!tolto);                                 /* non c'e' piu' */

    rubrica_free(r);
    rubrica_free(NULL);                             /* lecito */
    printf("test_rubrica: ok\n");
    return 0;
}
