/* str: stringhe composte sullo heap */
#ifndef STR_H
#define STR_H

#include <stdarg.h>

/* come sprintf, ma alloca quanto serve. Il risultato e' del chiamante (free);
   NULL se manca la memoria. */
char *str_printf(const char *fmt, ...) __attribute__((format(printf, 1, 2)));

/* la gemella con la va_list, per chi ha gia' i puntini */
char *str_vprintf(const char *fmt, va_list ap);

#endif
