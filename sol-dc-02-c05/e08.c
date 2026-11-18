/*
 * Esercizio 5.8 - Un registro a livelli
 *
 * log_scrivi(livello, file, riga, fmt, ...) scarta i messaggi sotto il
 * livello minimo e scrive gli altri su stderr come
 *     AVVISO e08.c:71: testo
 * Quattro macro, LOG_DEBUG ... LOG_ERRORE, aggiungono __FILE__ e __LINE__, che
 * solo una macro conosce (valgono nel punto della chiamata).
 *
 * Scelte: il livello minimo e' una variabile static del file, impostata con
 * log_livello(); i nomi dei livelli stanno in un array indicizzato dall'enum
 * con gli inizializzatori designati. Le macro hanno i puntini come unico
 * parametro, con il formato dentro: e' la forma che in C17 funziona anche
 * senza argomenti dopo il formato (LOG_INFO("avvio")).
 *
 * Errore tipico evitato: l'annotazione format(printf, m, n) conta i parametri
 * da 1. Qui fmt e' il QUARTO parametro e gli argomenti partono dal QUINTO:
 * format(printf, 4, 5). Con i numeri sbagliati il compilatore controllerebbe
 * `file` come formato, o non controllerebbe niente. La riga commentata nel
 * main, se la si scommenta, deve produrre l'avviso: e' la prova che il
 * controllo attraversa la macro.
 */
#include <stdarg.h>
#include <stdio.h>

typedef enum { DEBUG, INFO, AVVISO, ERRORE } Livello;

static const char *const NOMI[] = {
    [DEBUG] = "DEBUG", [INFO] = "INFO", [AVVISO] = "AVVISO", [ERRORE] = "ERRORE",
};

static Livello minimo = INFO;

void log_livello(Livello l) { minimo = l; }

__attribute__((format(printf, 4, 5)))
void log_scrivi(Livello livello, const char *file, int riga, const char *fmt, ...) {
    if (livello < minimo) return;
    va_list ap;
    va_start(ap, fmt);
    fprintf(stderr, "%-6s %s:%d: ", NOMI[livello], file, riga);
    vfprintf(stderr, fmt, ap);
    fputc('\n', stderr);
    va_end(ap);
}

#define LOG_DEBUG(...)  log_scrivi(DEBUG,  __FILE__, __LINE__, __VA_ARGS__)
#define LOG_INFO(...)   log_scrivi(INFO,   __FILE__, __LINE__, __VA_ARGS__)
#define LOG_AVVISO(...) log_scrivi(AVVISO, __FILE__, __LINE__, __VA_ARGS__)
#define LOG_ERRORE(...) log_scrivi(ERRORE, __FILE__, __LINE__, __VA_ARGS__)

int main(void) {
    int utenti = 3;
    LOG_DEBUG("questo non si vede: il minimo e' INFO");
    LOG_INFO("avvio");
    LOG_INFO("%d utenti collegati", utenti);
    LOG_AVVISO("nome troppo lungo: %s", "Massimiliano_il_Grande");

    log_livello(ERRORE);
    LOG_AVVISO("nemmeno questo: il minimo ora e' ERRORE");
    LOG_ERRORE("memoria esaurita dopo %d messaggi", 1000);

    log_livello(DEBUG);
    LOG_DEBUG("ora si': %s", "tutto visibile");
    /* LOG_INFO("%s utenti", utenti);      <- deve dare l'avviso di -Wformat */
    return 0;
}
