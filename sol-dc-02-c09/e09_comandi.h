/* comandi: interpreta una riga di testo e la esegue su una rubrica. E' il solo
   modulo che scrive sullo schermo. */
#ifndef E09_COMANDI_H
#define E09_COMANDI_H

#include <stdbool.h>

/* basta il nome del tipo: questo header non usa le funzioni della rubrica */
typedef struct Rubrica Rubrica;

/* esegue la riga (che viene modificata: si spezza sul posto) sulla rubrica.
   Restituisce false quando il comando chiede di terminare. */
bool comandi_esegui(Rubrica *r, char *riga);

#endif
