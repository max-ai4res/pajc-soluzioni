/*
 * Esercizio 7.8 - Lo studente e la copia profonda
 *
 * Idea: Studente contiene due puntatori, e il contratto dice che la memoria puntata e'
 * dello studente: nasce in studente_new, muore in studente_free. studente_copia e' una
 * copia PROFONDA: chiede blocchi nuovi (riusando studente_new, che alloca nome e voti
 * della misura giusta) e copia i contenuti con memcpy; da quel momento le due struct
 * possiedono blocchi diversi e si liberano entrambe senza che ASan dica nulla.
 * studente_new restituisce uno Studente con i puntatori a NULL e n = 0 se un malloc
 * fallisce, e il chiamante lo controlla.
 * Errore evitato: "Studente copia = *s; return copia;" e' una copia superficiale con il
 * nome di una profonda: compila, le medie tornano, e la seconda studente_free e' un
 * doppio free. Per vederlo: ./bin/e08 superficiale (dopo make asan) fa Studente b = a
 * e libera entrambe; ASan si ferma sulla seconda free con "attempting double-free".
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char *nome;        /* allocato: dello studente */
    int *voti;         /* allocato: dello studente */
    size_t n;
} Studente;

Studente studente_new(const char *nome, size_t n) {
    Studente s = { .nome = NULL, .voti = NULL, .n = 0 };
    size_t len = strlen(nome) + 1;
    s.nome = malloc(len);
    s.voti = calloc(n, sizeof *s.voti);
    if (s.nome == NULL || s.voti == NULL) {     /* uno dei due e' fallito: nessuno resta */
        free(s.nome);
        free(s.voti);
        s.nome = NULL;
        s.voti = NULL;
        return s;
    }
    memcpy(s.nome, nome, len);
    s.n = n;
    return s;
}

void studente_free(Studente *s) {
    free(s->nome);
    free(s->voti);
    s->nome = NULL;
    s->voti = NULL;
    s->n = 0;
}

Studente studente_copia(const Studente *s) {
    Studente c = studente_new(s->nome, s->n);    /* blocchi nuovi, lunghi quanto i suoi */
    if (c.nome != NULL)
        memcpy(c.voti, s->voti, s->n * sizeof *c.voti);
    return c;
}

double studente_media(const Studente *s) {
    if (s->n == 0) return 0;
    long somma = 0;
    for (size_t i = 0; i < s->n; i++) somma += s->voti[i];
    return (double)somma / (double)s->n;
}

static void studente_stampa(const char *etichetta, const Studente *s) {
    printf("%-10s %-6s voti:", etichetta, s->nome);
    for (size_t i = 0; i < s->n; i++) printf(" %d", s->voti[i]);
    printf("  media %.2f  (nome %p, voti %p)\n", studente_media(s),
           (void *)s->nome, (void *)s->voti);
}

int main(int argc, char **argv) {
    Studente greta = studente_new("Greta", 3);
    if (greta.nome == NULL) {
        fprintf(stderr, "memoria esaurita\n");
        return 1;
    }
    greta.voti[0] = 30;
    greta.voti[1] = 27;
    greta.voti[2] = 24;

    Studente copia = studente_copia(&greta);
    if (copia.nome == NULL) {
        fprintf(stderr, "memoria esaurita\n");
        studente_free(&greta);
        return 1;
    }
    copia.voti[2] = 18;                          /* cambia la copia, non l'originale */

    printf("Copia profonda: due struct, quattro blocchi diversi.\n");
    studente_stampa("originale", &greta);
    studente_stampa("copia", &copia);
    printf("media originale: attesa 27.00, ottenuta %.2f\n", studente_media(&greta));
    printf("media copia:     attesa 25.00, ottenuta %.2f\n", studente_media(&copia));

    if (argc > 1 && strcmp(argv[1], "superficiale") == 0) {
        printf("\nCopia superficiale: Studente b = greta copia i puntatori, non i blocchi.\n");
        Studente b = greta;
        studente_stampa("greta", &greta);
        studente_stampa("b", &b);
        studente_free(&copia);
        studente_free(&greta);                   /* b.nome e b.voti ora pendono */
        printf("libero anche b: doppio free, ASan lo segnala qui sotto\n");
        studente_free(&b);
        return 0;
    }

    studente_free(&greta);
    studente_free(&copia);
    printf("liberate entrambe: nome %p, voti %p\n", (void *)greta.nome, (void *)greta.voti);
    return 0;
}
