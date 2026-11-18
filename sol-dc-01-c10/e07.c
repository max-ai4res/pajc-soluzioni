/* Esercizio 10.7 - La rubrica completa
 * Il problema d'apertura chiuso: tre file (e07_rubrica.h, e07_rubrica.c, questo main)
 * piu' le macro in e07_util.h (ARRAY_LEN, SWAP_T, LOG). Contatto ha il nome allocato,
 * il telefono in un array fisso, un Recapito facoltativo come union discriminata
 * (NESSUNO, TELEFONO, EMAIL) e un uint8_t flags con la maschera FLAG_PREFERITO.
 * Rubrica e' l'array che cresce: rubrica_aggiungi mette il contatto in coda e lo fa
 * risalire al suo posto con SWAP_T(Contatto, ...), raddoppiando con realloc e la
 * variabile d'appoggio; rubrica_cerca restituisce un INDICE (SIZE_MAX se manca) con la
 * ricerca binaria, perche' un puntatore dentro l'array non sopravvive al realloc;
 * rubrica_rimuovi libera il nome, chiude il buco con memmove, poi decrementa n, in
 * quest'ordine; rubrica_free libera ogni nome e poi l'array. Le stringhe entrano con
 * strlen + 1 e memcpy, o con snprintf sulla misura del campo: mai strcpy.
 * Il main legge da stdin (anche da pipe) righe "nome telefono [email]" con fgets fino
 * a "fine", poi i comandi cerca, rimuovi, preferito, stampa, esci, con un messaggio su
 * ogni input non valido. Con l'argomento "genera N" costruisce N contatti in un ciclo,
 * in ordine casuale, e li cerca, ne rimuove un decimo e stampa: e' la prova con cento,
 * mille e diecimila contatti sotto make asan.
 * LOG (make debug-e07) a ogni raddoppio e a ogni rimozione.
 *     printf 'Greta 030111 greta@unibs.it\nMax 333222\nfine\nstampa\ncerca Max\nesci\n' | ./bin/e07
 *     make asan && ./bin/e07 genera 10000
 *
 * Il Makefile richiesto dall'esercizio, per i tre file chiamati rubrica.h, rubrica.c,
 * main.c (piu' util.h), nella forma del capitolo sulla toolchain:
 *
 *     CC      = gcc
 *     CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
 *     LDLIBS  = -lm
 *     SRC     = main.c rubrica.c
 *     OBJ     = $(SRC:.c=.o)
 *     HDR     = rubrica.h util.h
 *
 *     bin/rubrica: $(OBJ) | bin
 *     	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)
 *
 *     %.o: %.c $(HDR)
 *     	$(CC) $(CFLAGS) -c $<
 *
 *     bin:
 *     	mkdir -p bin
 *
 *     run: bin/rubrica
 *     	./bin/rubrica
 *
 *     debug: CFLAGS += -DDEBUG
 *     debug: clean bin/rubrica
 *
 *     asan: $(SRC) $(HDR) | bin
 *     	$(CC) $(CFLAGS) -fsanitize=address -o bin/rubrica $(SRC) $(LDLIBS)
 *
 *     clean:
 *     	rm -rf bin *.o
 *
 *     .PHONY: run debug asan clean
 *
 * (debug ricompila da zero perche' make non sa che CFLAGS e' cambiata; asan non passa
 * dagli oggetti perche' un .o senza strumentazione non si mescola a uno con.)
 * In questa cartella lo stesso programma si compila con make (bin/e07) e make debug-e07.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "e07_rubrica.h"
#include "e07_util.h"                         /* incluso due volte: la guardia lo permette */

enum { RIGA_MAX = 256 };

static bool telefono_valido(const char *s) {
    if (*s == '\0' || strlen(s) >= TELEFONO_MAX) return false;
    for (; *s != '\0'; s++)
        if (!isdigit((unsigned char)*s) && *s != '+' && *s != '-') return false;
    return true;
}

static bool memoria_esaurita(Rubrica *r) {
    fprintf(stderr, "memoria esaurita\n");
    rubrica_free(r);
    return false;
}

/* Fase 1: righe "nome telefono [recapito]" fino a "fine" (o alla fine dell'input). */
static bool leggi_contatti(Rubrica *r, FILE *in) {
    char riga[RIGA_MAX];
    size_t n_riga = 0;
    while (fgets(riga, sizeof riga, in) != NULL) {
        n_riga++;
        riga[strcspn(riga, "\n")] = '\0';
        char *nome = strtok(riga, " \t");
        if (nome == NULL) continue;                       /* riga vuota */
        if (strcmp(nome, "fine") == 0) return true;
        char *telefono = strtok(NULL, " \t");
        char *terzo = strtok(NULL, " \t");
        if (telefono == NULL) {
            printf("riga %zu: manca il telefono (nome telefono [email])\n", n_riga);
            continue;
        }
        if (!telefono_valido(telefono)) {
            printf("riga %zu: telefono non valido \"%s\" (cifre, + e -, meno di %d)\n",
                   n_riga, telefono, TELEFONO_MAX);
            continue;
        }
        Recapito rec = (terzo != NULL) ? recapito_da_testo(terzo) : recapito_nessuno();
        if (!rubrica_aggiungi(r, nome, telefono, rec)) return memoria_esaurita(r);
    }
    return true;                                          /* EOF senza "fine": va bene */
}

/* Fase 2: comandi cerca, rimuovi, preferito, stampa, esci. */
static void esegui_comandi(Rubrica *r, FILE *in) {
    char riga[RIGA_MAX];
    while (fgets(riga, sizeof riga, in) != NULL) {
        riga[strcspn(riga, "\n")] = '\0';
        char *cmd = strtok(riga, " \t");
        if (cmd == NULL) continue;
        char *arg = strtok(NULL, " \t");
        if (strcmp(cmd, "esci") == 0) return;
        if (strcmp(cmd, "stampa") == 0) {
            printf("%zu contatti (capacita' %zu):\n", r->n, r->cap);
            rubrica_stampa(r, stdout);
            continue;
        }
        bool vuole_nome = strcmp(cmd, "cerca") == 0 || strcmp(cmd, "rimuovi") == 0 ||
                          strcmp(cmd, "preferito") == 0;
        if (!vuole_nome) {
            printf("comando sconosciuto \"%s\": cerca, rimuovi, preferito, stampa, esci\n", cmd);
            continue;
        }
        if (arg == NULL) {
            printf("%s: manca il nome\n", cmd);
            continue;
        }
        size_t i = rubrica_cerca(r, arg);
        if (i == SIZE_MAX) {
            printf("%s non e' in rubrica\n", arg);
            continue;
        }
        if (strcmp(cmd, "cerca") == 0) {
            printf("trovato all'indice %zu: ", i);
            Rubrica uno = { .v = &r->v[i], .n = 1, .cap = 1 };   /* solo per stamparlo */
            rubrica_stampa(&uno, stdout);
        } else if (strcmp(cmd, "rimuovi") == 0) {
            rubrica_rimuovi(r, arg);
            printf("rimosso %s, restano %zu contatti\n", arg, r->n);
        } else {
            r->v[i].flags ^= FLAG_PREFERITO;                  /* accende o spegne */
            printf("%s %s preferito\n", arg,
                   (r->v[i].flags & FLAG_PREFERITO) ? "e' ora" : "non e' piu'");
        }
    }
}

/* La prova con N contatti generati in ordine casuale (permutazione con un generatore
 * lineare a seme fisso, cosi' l'inserimento ordinato lavora davvero). */
static int genera(size_t n) {
    size_t *perm = malloc(n * sizeof *perm);
    if (perm == NULL) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    for (size_t i = 0; i < n; i++) perm[i] = i;
    uint32_t seme = 12345u;
    for (size_t i = n - 1; i > 0; i--) {                      /* Fisher-Yates */
        seme = seme * 1103515245u + 12345u;
        size_t j = (seme >> 8) % (i + 1);
        SWAP_T(size_t, perm[i], perm[j]);
    }

    Rubrica r;
    if (!rubrica_init(&r, 2)) {
        free(perm);
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    static const char *const nomi[] = { "Greta", "Loretta", "Nami", "Max" };
    char nome[32], telefono[TELEFONO_MAX], email[EMAIL_MAX];
    for (size_t i = 0; i < n; i++) {
        size_t k = perm[i];
        snprintf(nome, sizeof nome, "%s%05zu", nomi[k % ARRAY_LEN(nomi)], k);
        snprintf(telefono, sizeof telefono, "030%07zu", k);
        Recapito rec = recapito_nessuno();
        if (k % 3 == 0) {
            snprintf(email, sizeof email, "%s%zu@unibs.it", nomi[k % ARRAY_LEN(nomi)], k);
            rec = recapito_da_testo(email);
        } else if (k % 3 == 1) {
            snprintf(email, sizeof email, "333%07zu", k);
            rec = recapito_da_testo(email);
        }
        if (!rubrica_aggiungi(&r, nome, telefono, rec)) {
            free(perm);
            memoria_esaurita(&r);
            return 1;
        }
        if (k % 7 == 0) {
            size_t i_nuovo = rubrica_cerca(&r, nome);         /* l'indice, dopo l'inserimento */
            r.v[i_nuovo].flags |= FLAG_PREFERITO;
        }
    }
    free(perm);
    perm = NULL;
    printf("generati %zu contatti: n = %zu, capacita' %zu\n", n, r.n, r.cap);

    /* l'ordine: ogni nome <= il successivo */
    size_t fuori_ordine = 0;
    for (size_t i = 1; i < r.n; i++)
        if (strcmp(r.v[i - 1].nome, r.v[i].nome) > 0) fuori_ordine++;
    printf("coppie fuori ordine: %zu (attese 0)\n", fuori_ordine);

    /* la ricerca: tutti presenti, uno assente */
    size_t trovati = 0;
    for (size_t k = 0; k < n; k++) {
        snprintf(nome, sizeof nome, "%s%05zu", nomi[k % ARRAY_LEN(nomi)], k);
        if (rubrica_cerca(&r, nome) != SIZE_MAX) trovati++;
    }
    printf("cercati %zu, trovati %zu; \"Nessuno\": %s\n", n, trovati,
           rubrica_cerca(&r, "Nessuno") == SIZE_MAX ? "SIZE_MAX, assente" : "ERRORE");

    /* le rimozioni: un contatto ogni dieci */
    size_t rimossi = 0;
    for (size_t k = 0; k < n; k += 10) {
        snprintf(nome, sizeof nome, "%s%05zu", nomi[k % ARRAY_LEN(nomi)], k);
        if (rubrica_rimuovi(&r, nome)) rimossi++;
    }
    printf("rimossi %zu, restano %zu (attesi %zu)\n", rimossi, r.n, n - rimossi);

    if (r.n <= 30) {
        rubrica_stampa(&r, stdout);
    } else {
        Rubrica testa = { .v = r.v, .n = 3, .cap = 3 };
        Rubrica coda = { .v = r.v + r.n - 3, .n = 3, .cap = 3 };
        rubrica_stampa(&testa, stdout);
        printf("  ...\n");
        rubrica_stampa(&coda, stdout);
    }
    rubrica_free(&r);
    rubrica_free(&r);                                         /* innocua la seconda volta */
    return 0;
}

int main(int argc, char *argv[]) {
    if (argc == 3 && strcmp(argv[1], "genera") == 0) {
        char *fine;
        unsigned long n = strtoul(argv[2], &fine, 10);
        if (fine == argv[2] || *fine != '\0' || n == 0) {
            fprintf(stderr, "uso: %s genera N   (N intero positivo)\n", argv[0]);
            return 1;
        }
        return genera((size_t)n);
    }
    if (argc != 1) {
        fprintf(stderr, "uso: %s            (legge da stdin)\n"
                        "     %s genera N   (N contatti generati)\n", argv[0], argv[0]);
        return 1;
    }

    Rubrica r;
    if (!rubrica_init(&r, 2)) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    printf("contatti, uno per riga: nome telefono [email o secondo telefono]; \"fine\" per chiudere\n");
    if (!leggi_contatti(&r, stdin)) return 1;
    printf("%zu contatti letti. Comandi: cerca NOME, rimuovi NOME, preferito NOME, stampa, esci\n", r.n);
    esegui_comandi(&r, stdin);
    rubrica_free(&r);
    return 0;
}
