/*
 * Esercizio 11.6 - Il debugging
 *
 * Questa e' la versione CORRETTA. Quella con i sei difetti e' nei tre sorgenti
 * e06_bug.c, e06_bug_misure.c/.h, e06_bug_testo.c/.h (make bug). Il programma
 * legge da un file righe "nome valore", le ordina per valore e stampa un
 * rapporto; i dati di prova sono in e06_misure.txt.
 *
 * IL DIARIO, nell'ordine in cui i difetti si incontrano.
 *
 * 1. LA DEFINIZIONE NELL'HEADER (cap. 9). `make bug` non arriva in fondo:
 *        duplicate symbol '_misure_totale'    (Linux: multiple definition)
 *    e06_bug_misure.h conteneva `size_t misure_totale = 0;`: una definizione,
 *    copiata in ognuno dei due sorgenti che includono l'header. La guardia non
 *    c'entra. Correzione: la variabile diventa static in e06_misure.c, e
 *    l'header dichiara una funzione misure_totale() che la legge. (Si
 *    trova senza eseguire niente: lo dice il linker.)
 *
 * 2. IL PROPRIETARIO DOPPIO (cap. 7). Collegato il programma, con ASan:
 *        ERROR: AddressSanitizer: heap-use-after-free ... in vsnprintf
 *        freed by thread T0 here: ... in main e06_bug.c:36
 *    (la lettura avviene quando testo_temp compone la riga con il nome)
 *    L'header promette che misure_aggiungi fa una COPIA del nome; il sorgente
 *    conservava il puntatore ricevuto. Il main, fidandosi del contratto,
 *    liberava la sua stringa subito dopo: l'elenco restava con puntatori
 *    pendenti, e misure_free li avrebbe liberati una seconda volta.
 *    Correzione: misure_aggiungi alloca e copia, come dice il commento. (Si
 *    poteva anche cambiare il contratto in "prende possesso": ma allora il
 *    main non doveva liberare, e andava detto nell'header.)
 *
 * 3. IL COMPARATORE CON L'OVERFLOW (cap. 3). Nessun errore, ma il rapporto e'
 *    in disordine: temperatura, quota, pressione, credito, debito, umidita;
 *    "credito 2000000000" viene PRIMA di "debito -2000000000".
 *    Il comparatore faceva `return x->valore - y->valore;`: la differenza di
 *    due int lontani non sta in un int. Con -fsanitize=undefined si vede
 *    anche il messaggio ("signed integer overflow"). Correzione:
 *    (x > y) - (x < y).
 *
 * 4. va_arg CON IL TIPO SBAGLIATO (cap. 5). La media dei tre valori piu'
 *    piccoli risultava 0.0 (su un'altra macchina, un altro numero senza
 *    senso). testo_media legge double, e il main
 *    le passava tre int: fra i puntini nessuno converte. Il compilatore non
 *    avvisa. Correzione: (double) davanti a ogni argomento nella chiamata.
 *    (L'alternativa migliore sarebbe una funzione che riceve un array.)
 *
 * 5. IL PUNTATORE DALL'ARENA DOPO L'AZZERAMENTO (cap. 7). L'ultima riga
 *    diceva "fine del umidita      65" invece di "fine del Rapporto di
 *    Greta": al posto del titolo, l'ultima riga stampata. Il
 *    titolo era una stringa temporanea di testo_temp, tenuta in una variabile
 *    per tutto il programma; ma il ciclo di stampa chiama testo_azzera a ogni
 *    riga, e il posto del titolo viene riusato. ASan non vede nulla:
 *    l'arena e' un array statico, sempre valido. Correzione: il titolo, che
 *    deve durare, viene copiato in un buffer del main.
 *
 * 6. L'assert CON L'EFFETTO COLLATERALE (cap. 10). Tutto a posto, finche' non
 *    si prova la variante di consegna (-O2 -DNDEBUG, make release-bug): il
 *    rapporto e' vuoto, "0 misure". L'aggiunta era scritta assert(misure_aggiungi(...)): con NDEBUG
 *    l'assert sparisce, e la chiamata con lui. Correzione: la chiamata fuori,
 *    il suo esito in una variabile, e un errore vero se fallisce (la memoria
 *    esaurita non e' un difetto del programma: non e' materia da assert).
 *
 * Che cosa ha trovato chi: il linker il primo; ASan il secondo; gli occhi
 * sull'output il terzo, il quarto e il quinto; una seconda variante di
 * compilazione il sesto. Gli avvisi del compilatore, questa volta, nessuno:
 * i sei difetti compilano puliti con -Wall -Wextra -Werror.
 */
#include <stdio.h>
#include <stdlib.h>

#include "e06_misure.h"
#include "e06_testo.h"

int main(int argc, char *argv[]) {
    FILE *f = argc > 1 ? fopen(argv[1], "r") : stdin;
    if (f == NULL) { perror(argv[1]); return 1; }

    Misure *m = misure_new();
    if (m == NULL) { if (f != stdin) fclose(f); return 1; }

    char titolo[64];                            /* deve durare: e' del main */
    snprintf(titolo, sizeof titolo, "Rapporto di %s", "Greta");

    int esito = 0;
    char nome[32];
    int valore;
    while (fscanf(f, "%31s %d", nome, &valore) == 2) {
        bool ok = misure_aggiungi(m, nome, valore);     /* fuori da ogni assert */
        if (!ok) { fprintf(stderr, "memoria esaurita\n"); esito = 1; break; }
    }
    if (f != stdin) fclose(f);

    misure_ordina(m);
    printf("%s\n", titolo);
    for (size_t i = 0; i < misure_quante(m); i++) {
        testo_azzera();                         /* una riga, una stringa temporanea */
        const Misura *x = misure_prendi(m, i);
        const char *riga = testo_temp("%-12s %d", x->nome, x->valore);
        printf("  %s\n", riga != NULL ? riga : "(riga troppo lunga)");
    }

    if (misure_quante(m) >= 3) {
        const Misura *a = misure_prendi(m, 0), *b = misure_prendi(m, 1), *c = misure_prendi(m, 2);
        printf("media delle tre piu' piccole: %.1f\n",
               testo_media(3, (double)a->valore, (double)b->valore, (double)c->valore));
    }
    printf("fine del %s (%zu misure, %zu aggiunte in tutto)\n",
           titolo, misure_quante(m), misure_totale());

    misure_free(m);
    return esito;
}
