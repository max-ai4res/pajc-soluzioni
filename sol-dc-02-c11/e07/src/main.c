/*
 * Esercizio 11.7 - chatc completo
 *
 * L'interprete dei comandi di una chat, da capo e per intero: il problema con
 * cui la dispensa si apre. Una riga che comincia con la barra e' un comando
 * (/nome, /chi, /a, /ordina, /storia, /aiuto, /esci), ogni altra riga un
 * messaggio; le risposte cominciano con una parola maiuscola.
 *
 *     make            costruisce bin/chatc
 *     make test       le prove dei moduli (tests/), fatte di assert
 *     make asan       tutto con AddressSanitizer, e le prove
 *     make release    -O2 -DNDEBUG
 *     ./bin/chatc [risposte.txt]     le risposte anche in un file
 *
 * Sei moduli e questo main, ciascuno con il suo capitolo:
 *   sessione   la tabella dei comandi (cap. 2), generata da un elenco solo con
 *              una X-macro (cap. 10) e cercata con bsearch (cap. 3); rispondi
 *              con il formato (cap. 5); l'arena per la riga, i contratti di
 *              possesso, la costruzione con una sola etichetta (cap. 7); il
 *              tipo opaco (cap. 9); un assert sull'ordine della tabella
 *   registro   gli utenti: tipo opaco, un Vettore di puntatori, i criteri di
 *              ordinamento in una tabella di comparatori (cap. 3)
 *   uscita     l'interfaccia con la vtable e quattro implementazioni:
 *              console, file, memoria, doppia (cap. 6)
 *   vettore    il contenitore generico di void * con la funzione che libera
 *              (cap. 4), come tipo opaco
 *   str        str_printf a due passi (cap. 7)
 *   terminale  cio' che dipende dal sistema, dietro una funzione (cap. 10)
 *
 * Il main non sa nulla di tutto questo: crea un'uscita, crea una sessione,
 * le consegna le righe, libera nell'ordine inverso.
 *
 * Errore tipico evitato: liberare l'uscita prima della sessione. La sessione
 * ha l'uscita in prestito: chi presta deve vivere piu' a lungo di chi riceve.
 */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#include "sessione.h"
#include "terminale.h"
#include "uscita.h"

int main(int argc, char *argv[]) {
    Uscita *u = uscita_console();
    if (u != NULL && argc > 1) {                /* ./chatc risposte.txt */
        Uscita *f = uscita_file(argv[1]);
        Uscita *d = f == NULL ? NULL : uscita_doppia(u, f);
        if (d == NULL) uscita_chiudi(f);
        else u = d;
    }
    Sessione *s = u == NULL ? NULL : sessione_new(u);
    if (s == NULL) { uscita_chiudi(u); return 1; }

    bool invito = terminale_interattivo();      /* "> " solo se c'e' qualcuno */
    char riga[256];
    while (!sessione_finita(s)) {
        if (invito) { fputs("> ", stdout); fflush(stdout); }
        if (fgets(riga, sizeof riga, stdin) == NULL) break;
        riga[strcspn(riga, "\n")] = '\0';
        if (riga[0] != '\0') sessione_elabora(s, riga);
    }
    sessione_free(s);                           /* prima la sessione, poi la sua uscita */
    uscita_chiudi(u);
    return 0;
}
