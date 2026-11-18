/* Esercizio 10.6 - Il debugging con ASan: IL PROGRAMMA CORRETTO E IL DIARIO
 * La lista della spesa di e06_bug.c senza i sei errori. Metodo: make asan-bug, poi
 * ./bin/e06_bug e06_spesa.txt, e a ogni fermata di ASan si corregge SOLO l'errore
 * segnalato e si rilancia (ASan si ferma al primo errore: gli altri compaiono dopo).
 * Le righe citate sono quelle di e06_bug.c.
 *
 * 1) riga 37-39, duplica: malloc(strlen(s)) e poi strcpy.
 *    ASan: heap-buffer-overflow, WRITE of size 5 ... #1 in duplica e06_bug.c:39,
 *          #2 in analizza_riga e06_bug.c:107; "0 bytes after 4-byte region allocated
 *          by ... duplica e06_bug.c:37".
 *    Causa: il blocco e' lungo quanto i caratteri, lo zero finale non ci sta: la copia
 *          di "pane" (4 lettere) scrive 5 byte in 4. Senza ASan funziona, perche' la
 *          libreria arrotonda i blocchi a 16 byte.
 *    Correzione: malloc(strlen(s) + 1) e memcpy di strlen + 1 byte. Non "un byte in
 *          piu' a occhio": il conto giusto.
 *
 * 2) riga 141, lista_carica, ramo "riga non valida": free(nome) dopo che
 *    analizza_riga (riga 111 o 117) aveva gia' liberato lo stesso blocco.
 *    ASan: attempting double-free on 0x6020000001b0 ... #1 in lista_carica
 *          e06_bug.c:141; "freed by ... analizza_riga e06_bug.c:117"; "previously
 *          allocated by ... duplica e06_bug.c:37". Scatta sulla riga 5 del file,
 *          "uova zero", l'unica che entra nel ramo d'errore.
 *    Causa: due funzioni credono entrambe di possedere il nome in caso d'errore. Senza
 *          ASan il programma muore qui con un trap della libreria (macOS) o con
 *          "free(): double free detected" (Linux), senza file ne' riga.
 *    Correzione: un solo proprietario. analizza_riga libera cio' che ha allocato E
 *          azzera *nome; il chiamante, nel ramo d'errore, non libera nulla. Il
 *          contratto e' scritto nel commento della funzione.
 *
 * 3) riga 90, lista_stampa: strcpy(etichetta, nome) con etichetta[16].
 *    ASan: stack-buffer-overflow, WRITE of size 30 ... #1 in lista_stampa
 *          e06_bug.c:90; "located in stack of thread T0 at offset 48 in frame".
 *    Causa: "biscottiIntegraliAlCioccolato" ha 29 caratteri piu' lo zero, il buffer
 *          ne tiene 16: strcpy non conosce la misura e scrive sulle variabili vicine.
 *    Correzione: snprintf(etichetta, sizeof etichetta, "%s", nome), che taglia e mette
 *          sempre lo zero. Non etichetta[32]: il prossimo nome sara' piu' lungo.
 *
 * 4) riga 182-183, main: nome_pane punta al nome dentro la lista, lista_rimuovi lo
 *    libera (riga 81), e la printf lo legge.
 *    ASan: heap-use-after-free, READ of size 2 ... #2 in main e06_bug.c:183; "freed by
 *          ... lista_rimuovi e06_bug.c:81, main e06_bug.c:182"; "previously allocated
 *          by ... duplica e06_bug.c:37, lista_aggiungi e06_bug.c:62".
 *    Causa: il nome e' della voce, e la voce muore con lista_rimuovi; chi teneva un
 *          puntatore a quel nome lo tiene a un blocco liberato. Senza ASan stampa
 *          "pane" (i byte sono ancora li') finche' qualcuno non riusa il blocco.
 *    Correzione: non si "evita" la printf: si capisce che il messaggio non deve
 *          dipendere da memoria della lista. Si stampa il nome chiesto, "pane",
 *          dall'esito di lista_rimuovi (bool), prima o dopo non importa.
 *
 * 5) riga 125-130, lista_carica: malloc(RIGA_MAX) e poi, se fopen fallisce, return
 *    senza free. ASan su macOS non vede i leak; letto nel codice, e confermato con
 *    leaks --atExit -- ./bin/e06_bug non_esiste.txt:
 *          "1 leak for 160 total leaked bytes ... ROOT LEAK: <malloc in lista_carica>".
 *    Su Linux ASan lo stampa alla chiusura (LeakSanitizer: detected memory leaks, con
 *    la riga della malloc).
 *    Causa: un ramo d'uscita che non percorre la pulizia finale.
 *    Correzione: aprire il file PRIMA di allocare, cosi' nel ramo d'errore non c'e'
 *          nulla da liberare (in alternativa free(riga) prima del return).
 *
 * 6) riga 53, lista_cresci: l->v = realloc(l->v, ...).
 *    ASan non lo segnala: e' un errore latente, fa danni solo se realloc fallisce
 *    (restituisce NULL, l->v viene sovrascritto e il blocco originale, ancora vivo,
 *    non ha piu' nessuno che lo possa liberare: leak senza rimedio, e la lista perde
 *    tutte le voci). Trovato leggendo il codice: e' la trappola classica.
 *    Correzione: la variabile d'appoggio: Voce *tmp = realloc(...); if (tmp == NULL)
 *          return false; l->v = tmp;.
 *
 * Dopo le quattro correzioni visibili ad ASan, make asan && ./bin/e06 e06_spesa.txt
 * termina pulito, e leaks --atExit non trova nulla neppure con il file mancante.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdint.h>

enum { RIGA_MAX = 128, CAP_INIZIALE = 4 };

typedef struct {
    char *nome;             /* allocato: della voce, liberato da lista_rimuovi o lista_free */
    int quantita;
} Voce;

typedef struct {
    Voce *v;
    size_t n, cap;
} Lista;

/* Copia di s sullo heap; il chiamante libera con free. */
static char *duplica(const char *s) {
    size_t n = strlen(s) + 1;               /* (1) i caratteri E lo zero */
    char *d = malloc(n);
    if (d == NULL) return NULL;
    memcpy(d, s, n);
    return d;
}

bool lista_init(Lista *l, size_t cap) {
    l->v = malloc(cap * sizeof *l->v);
    if (l->v == NULL) return false;
    l->n = 0;
    l->cap = cap;
    return true;
}

static bool lista_cresci(Lista *l) {
    size_t nuova = l->cap * 2;
    Voce *tmp = realloc(l->v, nuova * sizeof *tmp);   /* (6) la variabile d'appoggio */
    if (tmp == NULL) return false;          /* l->v e' ancora valido e pieno */
    l->v = tmp;
    l->cap = nuova;
    return true;
}

/* Aggiunge una voce in coda; il nome viene copiato (la lista possiede la copia). */
bool lista_aggiungi(Lista *l, const char *nome, int quantita) {
    if (l->n == l->cap && !lista_cresci(l)) return false;
    char *copia = duplica(nome);
    if (copia == NULL) return false;
    l->v[l->n].nome = copia;
    l->v[l->n].quantita = quantita;
    l->n++;
    return true;
}

/* L'indice della voce con quel nome, o SIZE_MAX. */
size_t lista_cerca(const Lista *l, const char *nome) {
    for (size_t i = 0; i < l->n; i++)
        if (strcmp(l->v[i].nome, nome) == 0) return i;
    return SIZE_MAX;
}

/* Toglie la voce con quel nome e ne libera il nome; false se non c'e'. Ogni puntatore
 * al nome di quella voce, dopo, e' pendente. */
bool lista_rimuovi(Lista *l, const char *nome) {
    size_t i = lista_cerca(l, nome);
    if (i == SIZE_MAX) return false;
    free(l->v[i].nome);                     /* prima il nome, poi il memmove */
    l->v[i].nome = NULL;
    memmove(&l->v[i], &l->v[i + 1], (l->n - i - 1) * sizeof l->v[0]);
    l->n--;
    return true;
}

void lista_stampa(const Lista *l) {
    for (size_t i = 0; i < l->n; i++) {
        char etichetta[16];
        snprintf(etichetta, sizeof etichetta, "%s", l->v[i].nome);   /* (3) taglia, con lo zero */
        for (char *c = etichetta; *c != '\0'; c++) *c = (char)toupper((unsigned char)*c);
        printf("  %-16s %3d\n", etichetta, l->v[i].quantita);
    }
}

/* Scrive la lista nel formato del file (una voce per riga). */
void lista_salva(const Lista *l, FILE *out) {
    for (size_t i = 0; i < l->n; i++)
        fprintf(out, "%s %d\n", l->v[i].nome, l->v[i].quantita);
}

/* Separa "nome quantita'": in *nome una copia allocata del nome (del chiamante, che la
 * libera), in *q la quantita'. Se la riga non e' valida restituisce false, e in quel
 * caso *nome e' NULL: tutto cio' che aveva allocato e' gia' stato liberato qui. */
static bool analizza_riga(char *riga, char **nome, int *q) {
    *nome = NULL;
    char *tok = strtok(riga, " \t");
    if (tok == NULL) return false;
    *nome = duplica(tok);
    if (*nome == NULL) return false;
    char *qs = strtok(NULL, " \t");
    char *fine = NULL;
    long v = (qs != NULL) ? strtol(qs, &fine, 10) : 0;
    if (qs == NULL || *fine != '\0' || v <= 0) {
        free(*nome);                        /* (2) libera E azzera: un solo proprietario */
        *nome = NULL;
        return false;
    }
    *q = (int)v;
    return true;
}

bool lista_carica(Lista *l, const char *percorso) {
    FILE *f = fopen(percorso, "r");         /* (5) prima il file: niente da liberare se fallisce */
    if (f == NULL) {
        perror(percorso);
        return false;
    }
    char *riga = malloc(RIGA_MAX);
    if (riga == NULL) {
        fclose(f);
        return false;
    }
    size_t n_riga = 0;
    while (fgets(riga, RIGA_MAX, f) != NULL) {
        n_riga++;
        riga[strcspn(riga, "\n")] = '\0';
        if (riga[0] == '\0') continue;
        char *nome = NULL;
        int q = 0;
        if (!analizza_riga(riga, &nome, &q)) {
            fprintf(stderr, "%s:%zu: riga non valida, ignorata\n", percorso, n_riga);
            continue;                       /* (2) analizza_riga ha gia' pulito */
        }
        if (!lista_aggiungi(l, nome, q))
            fprintf(stderr, "%s:%zu: memoria esaurita\n", percorso, n_riga);
        free(nome);                         /* la copia temporanea, del chiamante */
        nome = NULL;
    }
    fclose(f);
    free(riga);
    riga = NULL;
    return true;
}

void lista_free(Lista *l) {
    for (size_t i = 0; i < l->n; i++) free(l->v[i].nome);   /* ogni nome, poi l'array */
    free(l->v);
    l->v = NULL;
    l->n = l->cap = 0;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "uso: %s spesa.txt [salvataggio.txt]\n", argv[0]);
        return 1;
    }
    Lista l;
    if (!lista_init(&l, CAP_INIZIALE)) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    if (!lista_carica(&l, argv[1])) {
        lista_free(&l);
        return 1;
    }
    printf("lista caricata: %zu voci\n", l.n);
    lista_stampa(&l);

    size_t i = lista_cerca(&l, "latte");
    if (i != SIZE_MAX) printf("latte: %d\n", l.v[i].quantita);

    /* (4) il messaggio usa il nome chiesto e l'esito, non memoria della lista */
    if (lista_rimuovi(&l, "pane"))
        printf("rimosso: pane\n");
    else
        printf("pane non c'era\n");

    lista_aggiungi(&l, "burro", 1);
    printf("dopo rimozione e aggiunta: %zu voci\n", l.n);
    lista_stampa(&l);

    printf("--- salvataggio (formato del file) ---\n");
    lista_salva(&l, stdout);
    if (argc > 2) {
        FILE *out = fopen(argv[2], "w");
        if (out == NULL) {
            perror(argv[2]);
        } else {
            lista_salva(&l, out);
            fclose(out);
            printf("salvata in %s\n", argv[2]);
        }
    }
    lista_free(&l);
    return 0;
}
