/* str: stringhe composte sullo heap */
#include "str.h"

#include <stdio.h>
#include <stdlib.h>

char *str_vprintf(const char *fmt, va_list ap) {
    va_list ap2;
    va_copy(ap2, ap);
    int n = vsnprintf(NULL, 0, fmt, ap2);      /* primo passo: quanto e' lunga */
    va_end(ap2);
    if (n < 0) return NULL;
    char *s = malloc((size_t)n + 1);
    if (s != NULL) vsnprintf(s, (size_t)n + 1, fmt, ap);
    return s;
}

char *str_printf(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    char *s = str_vprintf(fmt, ap);
    va_end(ap);
    return s;
}
