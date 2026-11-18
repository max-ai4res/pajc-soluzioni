/* errore: le due tabelle, generate dallo stesso elenco dell'enum */
#include "errore.h"

#include <stddef.h>

/* secondo uso dell'elenco: i nomi, con # che trasforma il codice nel suo testo */
static const char *const NOMI[] = {
#define X(codice, messaggio) #codice,
    ERRORI_X
#undef X
};

/* terzo uso: i messaggi */
static const char *const MESSAGGI[] = {
#define X(codice, messaggio) messaggio,
    ERRORI_X
#undef X
};

/* le tabelle hanno tante righe quanti sono i codici: lo verifica il compilatore */
_Static_assert(sizeof NOMI / sizeof NOMI[0] == N_ERRORI, "NOMI e l'enum disallineati");
_Static_assert(sizeof MESSAGGI / sizeof MESSAGGI[0] == N_ERRORI, "MESSAGGI e l'enum disallineati");

/* un enum e' un intero: da fuori puo' arrivare qualunque valore */
static int valido(Errore e) { return (int)e >= 0 && (int)e < N_ERRORI; }

const char *errore_nome(Errore e)      { return valido(e) ? NOMI[e] : "ERR_?"; }
const char *errore_messaggio(Errore e) { return valido(e) ? MESSAGGI[e] : "errore sconosciuto"; }
