/* e08_util.h: macro comuni. Include e08_calc.h, cosi' chi include entrambi gli header
 * (e08.c) riceve e08_calc.h due volte: la guardia lo rende innocuo. */
#ifndef E08_UTIL_H
#define E08_UTIL_H

#include <stdio.h>

#include "e08_calc.h"

#define ARRAY_LEN(a) (sizeof (a) / sizeof (a)[0])

#ifdef DEBUG
#define LOG(msg) fprintf(stderr, "[%s:%d] %s\n", __FILE__, __LINE__, msg)
#else
#define LOG(msg) ((void)0)
#endif

/* quale dei tre eseguibili e' questo: lo decidono le macro della riga di comando */
#if defined(DEBUG)
#define BUILD_NOME "debug   (-DDEBUG -O0: LOG attivo, assert attivo, codice non ottimizzato)"
#elif defined(NDEBUG)
#define BUILD_NOME "release (-O2 -DNDEBUG: LOG spento, assert spento, codice ottimizzato)"
#else
#define BUILD_NOME "default (-g: LOG spento, assert attivo)"
#endif

#endif /* E08_UTIL_H */
