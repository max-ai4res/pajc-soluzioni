/* uscita: dove vanno le risposte. Un'interfaccia e quattro implementazioni. */
#ifndef USCITA_H
#define USCITA_H

#include <stdarg.h>

typedef struct Uscita Uscita;

/* la tabella che ogni implementazione riempie; e' pubblica perche' altri moduli
   possano aggiungere la loro */
typedef struct {
    void (*scrivi)(Uscita *self, const char *testo);
    void (*chiudi)(Uscita *self);
} UscitaVT;

struct Uscita { const UscitaVT *vt; };

/* i costruttori: l'uscita e' del chiamante (uscita_chiudi); NULL se falliscono */
Uscita *uscita_console(void);
Uscita *uscita_file(const char *percorso);
Uscita *uscita_memoria(void);
Uscita *uscita_doppia(Uscita *a, Uscita *b);        /* prende possesso di a e di b */

void uscita_scrivi(Uscita *u, const char *testo);
void uscita_vprintf(Uscita *u, const char *fmt, va_list ap);
void uscita_chiudi(Uscita *u);                      /* NULL non fa nulla */

/* solo per un'uscita creata con uscita_memoria: cio' che e' stato scritto finora.
   In prestito: vale fino alla prossima scrittura. */
const char *uscita_memoria_testo(const Uscita *u);

#endif
