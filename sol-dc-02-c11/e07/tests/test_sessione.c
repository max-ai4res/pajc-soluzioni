/* prove del modulo sessione: righe in ingresso, testo atteso in uscita */
#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "sessione.h"
#include "uscita.h"

/* esegue le righe su una sessione nuova e confronta le risposte con quelle attese */
static void prova(const char *nome, const char *righe[], size_t n, const char *attese) {
    Uscita *u = uscita_memoria();
    assert(u != NULL);
    Sessione *s = sessione_new(u);
    assert(s != NULL);
    for (size_t i = 0; i < n; i++) sessione_elabora(s, righe[i]);
    if (strcmp(uscita_memoria_testo(u), attese) != 0) {
        printf("test_sessione: %s FALLITA\n--- attese ---\n%s--- ottenute ---\n%s",
               nome, attese, uscita_memoria_testo(u));
        assert(false);
    }
    sessione_free(s);
    uscita_chiudi(u);
}

#define PROVA(nome, attese, ...)                                        \
    do {                                                                \
        const char *righe_[] = { __VA_ARGS__ };                         \
        prova(nome, righe_, sizeof righe_ / sizeof righe_[0], attese);  \
    } while (0)

int main(void) {
    PROVA("un messaggio",
          "BENVENUTO Greta\nMSG Greta ciao a tutti\n",
          "/nome Greta", "ciao a tutti");

    PROVA("prima il nome",
          "ERR prima /nome\nERR prima /nome\n",
          "ciao", "/a Greta ciao");

    PROVA("gli argomenti",
          "ERR uso: /nome N     il nome, per primo\n"
          "ERR uso: /chi        chi e' collegato\n"
          "ERR comando sconosciuto: /balla\n",
          "/nome", "/chi adesso", "/balla");

    PROVA("privato",
          "BENVENUTO Nami\nBENVENUTO Greta\nPRIV Greta Nami ci vediamo  dopo\n"
          "ERR non c'e': Max\n",
          "/nome Nami", "/nome Greta", "/a Nami ci vediamo  dopo", "/a Max ciao");

    PROVA("chi e ordina",
          "BENVENUTO Nami\nMSG Nami uno\nMSG Nami due\nBENVENUTO Greta\nMSG Greta tre\n"
          "UTENTI Greta Nami\nINFO ordine per messaggi\nUTENTI Nami Greta\n"
          "ERR criterio sconosciuto: eta\n",
          "/nome Nami", "uno", "due", "/nome Greta", "tre",
          "/chi", "/ordina messaggi", "/chi", "/ordina eta");

    PROVA("storia",
          "INFO nessun messaggio\nBENVENUTO Greta\nMSG Greta ciao\n"
          "INFO 1 Greta ciao\nCIAO\n",
          "/storia", "/nome Greta", "ciao", "/storia", "/esci");

    printf("test_sessione: ok\n");
    return 0;
}
