/*
 * Esercizio 2.9 - Il semaforo
 *
 * Una macchina a stati scritta come tabella: una riga per stato, con il nome
 * e la funzione che, dato un evento, restituisce lo stato successivo. Il ciclo
 * principale non contiene nessuno switch sullo stato: fa
 *     stato = STATI[stato].prossimo(evento);
 * e la funzione giusta la sceglie l'indice.
 *
 * Eventi: 't' scade il tempo (rosso -> verde -> giallo -> rosso), 'g' guasto
 * (da qualunque stato a lampeggiante), 'r' riparato (da lampeggiante a rosso).
 * Ogni funzione conosce solo gli eventi che la riguardano; per gli altri
 * restituisce il proprio stato.
 *
 * Scelte: lo stato e' un enum usato come indice, e la tabella e' inizializzata
 * con gli indici designati ([ROSSO] = ...), cosi' l'ordine delle righe non
 * puo' disallinearsi da quello dell'enum. N_STATI, ultimo valore dell'enum,
 * dimensiona l'array.
 *
 * Errore tipico evitato: scrivere la tabella "in ordine" senza gli indici e
 * poi inserire uno stato in mezzo all'enum: ogni stato eseguirebbe la
 * funzione di un altro, senza errori di compilazione.
 */
#include <stdio.h>

typedef enum { ROSSO, VERDE, GIALLO, LAMPEGGIANTE, N_STATI } Stato;

static Stato da_rosso(char e)  { return e == 't' ? VERDE  : e == 'g' ? LAMPEGGIANTE : ROSSO;  }
static Stato da_verde(char e)  { return e == 't' ? GIALLO : e == 'g' ? LAMPEGGIANTE : VERDE;  }
static Stato da_giallo(char e) { return e == 't' ? ROSSO  : e == 'g' ? LAMPEGGIANTE : GIALLO; }
static Stato da_lampeggiante(char e) { return e == 'r' ? ROSSO : LAMPEGGIANTE; }

typedef struct {
    const char *nome;
    Stato (*prossimo)(char evento);
} RigaStato;

static const RigaStato STATI[N_STATI] = {
    [ROSSO]        = { "rosso",        da_rosso        },
    [VERDE]        = { "verde",        da_verde        },
    [GIALLO]       = { "giallo",       da_giallo       },
    [LAMPEGGIANTE] = { "lampeggiante", da_lampeggiante },
};

int main(void) {
    Stato stato = ROSSO;
    char evento;
    printf("eventi: t (tempo), g (guasto), r (riparato); stato: %s\n", STATI[stato].nome);
    while (scanf(" %c", &evento) == 1) {
        stato = STATI[stato].prossimo(evento);
        printf("%c -> %s\n", evento, STATI[stato].nome);
    }
    return 0;
}
