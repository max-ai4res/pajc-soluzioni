# Soluzioni dei problemi del capitolo 3 di DC-01 "C Fast Forward"
#
# `make` costruisce bin/e06..e08. L'esercizio 3.7 muore di stack overflow solo
# se lanciato con l'argomento `crash` (./bin/e07 crash).

CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SRC     = $(wildcard e0?.c)
BIN     = $(patsubst %.c,bin/%,$(SRC))

all: $(BIN)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin:
	mkdir -p bin

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf bin

.PHONY: all asan clean
