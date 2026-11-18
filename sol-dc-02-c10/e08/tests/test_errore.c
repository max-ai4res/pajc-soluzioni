/* prove del modulo errore */
#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "errore.h"

int main(void) {
    assert(ERR_OK == 0);                            /* il primo dell'elenco vale zero */
    assert(N_ERRORI == 5);

    for (int e = 0; e < N_ERRORI; e++) {            /* ogni codice ha nome e messaggio */
        const char *nome = errore_nome((Errore)e);
        const char *messaggio = errore_messaggio((Errore)e);
        assert(nome != NULL && strncmp(nome, "ERR_", 4) == 0);
        assert(messaggio != NULL && messaggio[0] != '\0');
        for (int f = 0; f < e; f++)                 /* e i nomi sono tutti diversi */
            assert(strcmp(nome, errore_nome((Errore)f)) != 0);
    }
    assert(strcmp(errore_nome(ERR_FILE), "ERR_FILE") == 0);
    assert(strcmp(errore_messaggio(ERR_MEMORIA), "memoria esaurita") == 0);

    assert(strcmp(errore_nome((Errore)99), "ERR_?") == 0);          /* fuori intervallo */
    assert(strcmp(errore_nome((Errore)-1), "ERR_?") == 0);
    assert(strcmp(errore_messaggio((Errore)N_ERRORI), "errore sconosciuto") == 0);

    printf("test_errore: ok\n");
    return 0;
}
