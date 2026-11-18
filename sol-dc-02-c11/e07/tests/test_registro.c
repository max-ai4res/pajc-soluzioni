/* prove del modulo registro */
#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#include "registro.h"

/* raccoglie i nomi nell'ordine in cui arrivano: "Greta Nami " */
static void raccogli(const Utente *u, void *ctx) {
    char *testo = ctx;
    strcat(testo, utente_nome(u));
    strcat(testo, " ");
}

int main(void) {
    Registro *r = registro_new();
    assert(r != NULL);
    assert(registro_quanti(r) == 0);
    assert(registro_cerca(r, "Greta") == NULL);

    Utente *nami = registro_aggiungi(r, "Nami");
    Utente *greta = registro_aggiungi(r, "Greta");
    assert(nami != NULL && greta != NULL);
    assert(registro_quanti(r) == 2);
    assert(registro_cerca(r, "Greta") == greta);
    assert(strcmp(utente_nome(greta), "Greta") == 0);
    assert(utente_messaggi(greta) == 0);

    utente_ha_scritto(nami);
    utente_ha_scritto(nami);
    utente_ha_scritto(greta);
    assert(utente_messaggi(nami) == 2);

    char testo[64] = "";
    registro_per_ognuno(r, raccogli, testo);
    assert(strcmp(testo, "Greta Nami ") == 0);          /* per nome, all'inizio */

    bool cambiato = registro_ordina_per(r, "messaggi");   /* fuori dall'assert */
    assert(cambiato);
    testo[0] = '\0';
    registro_per_ognuno(r, raccogli, testo);
    assert(strcmp(testo, "Nami Greta ") == 0);

    cambiato = registro_ordina_per(r, "eta");           /* non esiste: non cambia */
    assert(!cambiato);
    assert(registro_cerca(r, "Nami") == nami);          /* i prestiti reggono all'ordinamento */

    registro_free(r);
    printf("test_registro: ok\n");
    return 0;
}
