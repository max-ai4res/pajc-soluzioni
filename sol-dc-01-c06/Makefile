CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SRC     = $(wildcard e0?.c)
BIN     = $(patsubst %.c,bin/%,$(SRC))

# e09_quattro.c e' il programma con i quattro errori dell'esercizio 6.9:
# non lo prende $(wildcard e0?.c), quindi ha la sua regola e sta in all.
all: $(BIN) bin/e09_quattro

bin/e09_quattro: e09_quattro.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin:
	mkdir -p bin

asan: CFLAGS += -fsanitize=address
asan: clean all

# solo il programma con gli errori, con ASan: ./bin/e09_quattro li mostra
asan-quattro: CFLAGS += -fsanitize=address
asan-quattro: | bin
	rm -f bin/e09_quattro
	$(CC) $(CFLAGS) -o bin/e09_quattro e09_quattro.c $(LDLIBS)

clean:
	rm -rf bin

.PHONY: all asan asan-quattro clean
