/*
 * Esercizio 2.8 - Il menu generato
 *
 * Le voci del menu stanno in VOCI: tasto, descrizione, azione. Il programma
 * stampa il menu percorrendo la tabella, legge un tasto, cerca la riga ed
 * esegue l'azione. Aggiungere una voce e' scrivere una funzione e una riga:
 * la stampa del menu e la ricerca non si toccano.
 *
 * Scelte: le azioni hanno firma void (*)(void) come chiede l'enunciato, quindi
 * lo stato su cui lavorano (il contatore, il flag di uscita) e' in due
 * variabili static del file. E' il limite di una callback senza parametri:
 * il capitolo 3 mostra il rimedio, un parametro di contesto. La voce 'q' e'
 * una riga come le altre, la cui azione alza il flag che il ciclo controlla.
 *
 * Errore tipico evitato: chiamare exit() dentro l'azione di uscita. Funziona,
 * ma il main non puo' piu' fare nulla dopo il ciclo (salutare, liberare
 * risorse). Qui l'azione cambia uno stato e il ciclo termina da solo.
 */
#include <stdbool.h>
#include <stdio.h>

static int contatore = 0;
static bool finito = false;

static void incrementa(void) { contatore++; }
static void decrementa(void) { contatore--; }
static void azzera(void)     { contatore = 0; }
static void stampa(void)     { printf("contatore = %d\n", contatore); }
static void esci(void)       { finito = true; }

typedef struct {
    char tasto;
    const char *descrizione;
    void (*azione)(void);
} Voce;

static const Voce VOCI[] = {
    { '+', "incrementa", incrementa },
    { '-', "decrementa", decrementa },
    { '0', "azzera",     azzera     },
    { 'p', "stampa",     stampa     },
    { 'q', "esci",       esci       },
};
#define N_VOCI (sizeof VOCI / sizeof VOCI[0])

static void stampa_menu(void) {
    for (size_t i = 0; i < N_VOCI; i++)
        printf("  %c  %s\n", VOCI[i].tasto, VOCI[i].descrizione);
    printf("> ");
}

static const Voce *cerca(char tasto) {
    for (size_t i = 0; i < N_VOCI; i++)
        if (VOCI[i].tasto == tasto) return &VOCI[i];
    return NULL;
}

int main(void) {
    char tasto;
    while (!finito) {
        stampa_menu();
        if (scanf(" %c", &tasto) != 1) break;      /* fine dell'input */
        const Voce *v = cerca(tasto);
        if (v == NULL) printf("tasto sconosciuto: %c\n", tasto);
        else v->azione();
    }
    printf("fine, contatore = %d\n", contatore);
    return 0;
}
