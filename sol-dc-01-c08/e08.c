/*
 * Esercizio 8.8 - Il recapito
 *
 * Idea: Recapito e' una union discriminata: un enum dice quale dei due campi della
 * union vale, e ogni accesso alla union passa da uno switch sul tipo. I costruttori
 * recapito_telefono e recapito_email restituiscono per valore un Recapito gia'
 * coerente (discriminante impostato e campo copiato con snprintf nella misura del
 * buffer), cosi' non esiste un istante in cui tipo e contenuto non combaciano.
 * NESSUNO e' il terzo stato: nessun campo della union vale, e recapito_valido lo
 * rifiuta. La validita' e' minima: un'email ha una @ (strchr), un telefono non e'
 * vuoto e ha solo cifre e spazi (isdigit su unsigned char).
 * sizeof(Recapito) e' 52: 4 per l'enum, 48 per la union grande quanto email.
 * Errore evitato: leggere r->u.email quando il tipo e' TELEFONO non e' un errore per il
 * linguaggio, da' sedici byte di telefono e trentadue di spazzatura senza uno zero
 * garantito; lo switch sul tipo prima di ogni accesso e' l'unica protezione.
 */
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

typedef enum { NESSUNO, TELEFONO, EMAIL } TipoRecapito;

typedef struct {
    TipoRecapito tipo;            /* il discriminante: quale campo vale */
    union {
        char telefono[16];
        char email[48];
    } u;
} Recapito;

Recapito recapito_nessuno(void) {
    return (Recapito){ .tipo = NESSUNO };
}

Recapito recapito_telefono(const char *telefono) {
    Recapito r = { .tipo = TELEFONO };
    snprintf(r.u.telefono, sizeof r.u.telefono, "%s", telefono);
    return r;
}

Recapito recapito_email(const char *email) {
    Recapito r = { .tipo = EMAIL };
    snprintf(r.u.email, sizeof r.u.email, "%s", email);
    return r;
}

void recapito_stampa(const Recapito *r) {
    switch (r->tipo) {
    case NESSUNO:  printf("%-6s %-24s", "-", "(nessun recapito)"); break;
    case TELEFONO: printf("%-6s %-24s", "tel.", r->u.telefono);    break;
    case EMAIL:    printf("%-6s %-24s", "email", r->u.email);      break;
    }
}

bool recapito_valido(const Recapito *r) {
    switch (r->tipo) {
    case NESSUNO:
        return false;
    case TELEFONO: {
        const char *s = r->u.telefono;
        if (*s == '\0') return false;
        for (; *s != '\0'; s++)
            if (!isdigit((unsigned char)*s) && *s != ' ') return false;
        return true;
    }
    case EMAIL:
        return strchr(r->u.email, '@') != NULL;
    }
    return false;
}

int main(void) {
    const char *nomi[] = { "Greta", "Loretta", "Max", "Nami", "Anna", "Bruno" };
    Recapito recapiti[] = {
        recapito_telefono("030 1234567"),         /* valido */
        recapito_email("loretta@unibs.it"),       /* valido */
        recapito_telefono("333-98 76 543"),       /* non valido: un trattino */
        recapito_email("nami.unibs.it"),          /* non valido: manca la @ */
        recapito_nessuno(),                       /* non valido: nessun recapito */
        recapito_email("max@studenti.unibs.it"),  /* valido */
    };
    const char *attesi[] = { "si", "si", "no", "no", "no", "si" };
    size_t n = sizeof recapiti / sizeof recapiti[0];

    printf("%-8s %-6s %-24s %s\n", "nome", "tipo", "recapito", "valido (atteso/ottenuto)");
    for (size_t i = 0; i < n; i++) {
        printf("%-8s ", nomi[i]);
        recapito_stampa(&recapiti[i]);
        printf(" %s/%s\n", attesi[i], recapito_valido(&recapiti[i]) ? "si" : "no");
    }

    printf("\nsizeof(Recapito) = %zu: atteso 52 (4 di enum + 48 di union, non 4 + 16 + 48 = 68)\n",
           sizeof(Recapito));
    return 0;
}
