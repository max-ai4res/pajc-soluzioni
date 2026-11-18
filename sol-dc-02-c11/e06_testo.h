/* testo: una media variadica e le stringhe temporanee del rapporto */
#ifndef E06_TESTO_H
#define E06_TESTO_H

/* la media di n valori, che devono essere passati come double */
double testo_media(int n, ...);

/* una stringa temporanea composta con un formato. In prestito: vale fino alla
   prossima testo_azzera(). NULL se non c'e' piu' spazio. */
const char *testo_temp(const char *fmt, ...) __attribute__((format(printf, 1, 2)));

/* rende di nuovo disponibile lo spazio delle stringhe temporanee: tutte quelle
   ottenute finora da testo_temp non valgono piu' */
void testo_azzera(void);

#endif
