# Soluzioni dei problemi del capitolo 8 di DC-02 "Funzioni, memoria e moduli"
#
# `make` costruisce bin/e08 (il solo problema in C). I problemi 8.6 e 8.7 sono
# programmi Java in un file solo, che si lanciano senza compilare (serve un JDK 21
# o successivo): `make run-e06`, `make run-e07`.

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

# la profondita' dello stack, con tre dimensioni
run-e06:
	java -Xss256k e06.java
	java e06.java
	java -Xss4m e06.java

# il leak e le due cure, nello stesso heap di 32 MB
run-e07:
	java -Xmx32m e07.java

clean:
	rm -rf bin

.PHONY: all asan run-e06 run-e07 clean
