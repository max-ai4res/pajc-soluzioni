/* contatto: un nome e un telefono. Tipo pubblico, piccolo, per valore. */
#ifndef E09_CONTATTO_H
#define E09_CONTATTO_H

typedef struct { char nome[32]; char telefono[20]; } Contatto;

/* un contatto con una copia (troncata se serve) di nome e telefono */
Contatto contatto_new(const char *nome, const char *telefono);

/* per nome: negativo, zero, positivo */
int contatto_confronta(const Contatto *a, const Contatto *b);

#endif
