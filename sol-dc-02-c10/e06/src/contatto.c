/* contatto: un nome e un telefono */
#include "contatto.h"

#include <stdio.h>
#include <string.h>

Contatto contatto_new(const char *nome, const char *telefono) {
    Contatto c;
    snprintf(c.nome, sizeof c.nome, "%s", nome);
    snprintf(c.telefono, sizeof c.telefono, "%s", telefono);
    return c;
}

int contatto_confronta(const Contatto *a, const Contatto *b) {
    return strcmp(a->nome, b->nome);
}
