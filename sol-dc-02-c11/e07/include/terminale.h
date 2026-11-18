/* terminale: cio' che dipende dal sistema operativo, in un posto solo */
#ifndef TERMINALE_H
#define TERMINALE_H

#include <stdbool.h>

/* vero se l'ingresso e' una tastiera, falso se e' un file o una pipe */
bool terminale_interattivo(void);

#endif
