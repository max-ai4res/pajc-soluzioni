# Soluzioni dei problemi del capitolo 2 di DC-01 "C Fast Forward"
#
# `make` costruisce tutti gli eseguibili in bin/ (il primo bersaglio e' `all`:
# se fosse bin/e06, gli altri non verrebbero fatti). Gli esercizi 2.7 e 2.8
# sono a piu' file e hanno regole esplicite, prima della regola con il pattern.
#
# Esercizio 2.9: `make debug` rifa' bin/e09 con -DDEBUG, `make asan` rifa' tutto
# con -fsanitize=address, `make run` lancia bin/e09. Dopo `make debug`, `make`
# da solo non ricompila: bin/e09 e' piu' recente di e09.c e per make e' aggiornato,
# perche' make guarda le date e non sa con quali flag un file e' stato prodotto.
# Per questo debug cancella prima bin/e09 (e asan passa da clean).

CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SRC     = $(wildcard e0?.c)
BIN     = $(patsubst %.c,bin/%,$(SRC)) bin/e08_prodotto

all: $(BIN)

# 2.7: modulo tabella
bin/e07: e07.c e07_tabella.c e07_tabella.h | bin
	$(CC) $(CFLAGS) -o $@ e07.c e07_tabella.c $(LDLIBS)

# 2.8: due programmi, un modulo leggi condiviso
bin/e08: e08.c e08_leggi.c e08_leggi.h | bin
	$(CC) $(CFLAGS) -o $@ e08.c e08_leggi.c $(LDLIBS)

bin/e08_prodotto: e08_prodotto.c e08_leggi.c e08_leggi.h | bin
	$(CC) $(CFLAGS) -o $@ e08_prodotto.c e08_leggi.c $(LDLIBS)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin:
	mkdir -p bin

# 2.9: stesso eseguibile, flag diversi; debug cancella solo bin/e09 e lo rifa'
debug: CFLAGS += -DDEBUG
debug: rimuovi-e09 bin/e09

rimuovi-e09:
	rm -f bin/e09

run: bin/e09
	./bin/e09

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf bin

.PHONY: all asan debug rimuovi-e09 run clean
