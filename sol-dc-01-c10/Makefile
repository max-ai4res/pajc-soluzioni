CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SRC     = $(wildcard e0?.c)
BIN     = $(patsubst %.c,bin/%,$(SRC))

all: $(BIN) bin/e06_bug

# Esercizi in piu' file: regole esplicite, prima della regola con il pattern.
bin/e03: e03.c e03_matrice.c e03_matrice.h | bin
	$(CC) $(CFLAGS) -o $@ e03.c e03_matrice.c $(LDLIBS)

bin/e06_bug: e06_bug.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin/e07: e07.c e07_rubrica.c e07_rubrica.h e07_util.h | bin
	$(CC) $(CFLAGS) -o $@ e07.c e07_rubrica.c $(LDLIBS)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin:
	mkdir -p bin

asan: CFLAGS += -fsanitize=address
asan: clean all

# Solo il programma con gli errori (esercizio 10.6), strumentato con ASan:
#   make asan-bug && ./bin/e06_bug e06_spesa.txt
asan-bug: e06_bug.c | bin
	$(CC) $(CFLAGS) -fsanitize=address -o bin/e06_bug $< $(LDLIBS)

# La rubrica (esercizio 10.7) con LOG acceso: make debug-e07 && ./bin/e07 genera 100
debug-e07: e07.c e07_rubrica.c e07_rubrica.h e07_util.h | bin
	$(CC) $(CFLAGS) -DDEBUG -o bin/e07 e07.c e07_rubrica.c $(LDLIBS)

clean:
	rm -rf bin

.PHONY: all asan asan-bug debug-e07 clean
