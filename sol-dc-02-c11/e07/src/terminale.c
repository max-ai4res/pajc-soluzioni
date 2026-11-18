/* terminale: cio' che dipende dal sistema operativo, in un posto solo */
#if defined(_WIN32)
#  include <io.h>
#  define E_UN_TERMINALE(fd) _isatty(fd)
#elif defined(__unix__) || defined(__APPLE__)
#  define _POSIX_C_SOURCE 200809L        /* isatty non e' C standard: e' POSIX */
#  include <unistd.h>
#  define E_UN_TERMINALE(fd) isatty(fd)
#else
#  error "sistema non previsto: scrivere qui la versione di E_UN_TERMINALE"
#endif

#include "terminale.h"

bool terminale_interattivo(void) {
    return E_UN_TERMINALE(0) != 0;         /* 0: l'ingresso standard */
}
