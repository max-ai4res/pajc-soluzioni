/* prove del modulo contatto */
#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "contatto.h"

int main(void) {
    Contatto a = contatto_new("Greta", "347-9999");
    Contatto b = contatto_new("Nami", "333-1111");
    assert(strcmp(a.nome, "Greta") == 0);
    assert(strcmp(a.telefono, "347-9999") == 0);
    assert(contatto_confronta(&a, &b) < 0);
    assert(contatto_confronta(&b, &a) > 0);
    assert(contatto_confronta(&a, &a) == 0);

    /* un nome troppo lungo viene troncato, e resta una stringa valida */
    Contatto lungo = contatto_new("Massimiliano_Alessandro_Giovanni_Battista", "0");
    assert(strlen(lungo.nome) == sizeof lungo.nome - 1);

    printf("test_contatto: ok\n");
    return 0;
}
