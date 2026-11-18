/* errore: i codici d'errore del programma, con nome e messaggio */
#ifndef ERRORE_H
#define ERRORE_H

/* l'elenco, scritto una volta: codice e messaggio. Da qui escono l'enum (qui
   sotto) e le due tabelle di errore.c. Per aggiungere un errore: una riga. */
#define ERRORI_X                                                \
    X(ERR_OK,        "nessun errore")                           \
    X(ERR_MEMORIA,   "memoria esaurita")                        \
    X(ERR_FILE,      "impossibile aprire il file")              \
    X(ERR_FORMATO,   "formato dei dati non valido")             \
    X(ERR_ARGOMENTO, "argomento non valido")

/* primo uso: i valori dell'enum, nell'ordine dell'elenco; N_ERRORI li conta */
typedef enum {
#define X(codice, messaggio) codice,
    ERRORI_X
#undef X
    N_ERRORI
} Errore;

/* il nome del codice ("ERR_FILE"); "ERR_?" se il codice non esiste.
   In prestito: e' una stringa che vive quanto il programma. */
const char *errore_nome(Errore e);

/* la descrizione; "errore sconosciuto" se il codice non esiste. In prestito. */
const char *errore_messaggio(Errore e);

#endif
