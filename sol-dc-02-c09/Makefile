# Soluzioni dei problemi del capitolo 9 di DC-02 "Funzioni, memoria e moduli"
#
# Ogni problema e' un programma in piu' file: il main in eNN.c, i moduli in
# eNN_nome.c / eNN_nome.h. `make` costruisce bin/e06, bin/e07 (due eseguibili:
# stesso main, due implementazioni dello stesso header), bin/e08, bin/e09.

CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm

all: bin/e06 bin/e07_lineare bin/e07_ordinato bin/e08 bin/e09

bin/e06: e06.c e06_frazione.c e06_frazione.h | bin
	$(CC) $(CFLAGS) -o $@ e06.c e06_frazione.c $(LDLIBS)

# lo stesso main con l'una o con l'altra implementazione di e07_contatore.h
bin/e07_lineare: e07.c e07_contatore_lineare.c e07_contatore.h | bin
	$(CC) $(CFLAGS) -o $@ e07.c e07_contatore_lineare.c $(LDLIBS)

bin/e07_ordinato: e07.c e07_contatore_ordinato.c e07_contatore.h | bin
	$(CC) $(CFLAGS) -o $@ e07.c e07_contatore_ordinato.c $(LDLIBS)

bin/e08: e08.c e08_vettore.c e08_vettore.h | bin
	$(CC) $(CFLAGS) -o $@ e08.c e08_vettore.c $(LDLIBS)

E09 = e09.c e09_contatto.c e09_rubrica.c e09_comandi.c
bin/e09: $(E09) e09_contatto.h e09_rubrica.h e09_comandi.h | bin
	$(CC) $(CFLAGS) -o $@ $(E09) $(LDLIBS)

# che cosa esporta il modulo vettore: solo i nomi dell'header (lettera T)
nomi-e08: | bin
	$(CC) $(CFLAGS) -c e08_vettore.c -o bin/e08_vettore.o
	nm bin/e08_vettore.o | grep -i ' t '

bin:
	mkdir -p bin

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf bin

.PHONY: all asan nomi-e08 clean
