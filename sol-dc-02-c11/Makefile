# Soluzioni dei problemi del capitolo 11 (Palestra) di DC-02 "Funzioni, memoria e moduli"
#
# `make` costruisce bin/e01 ... bin/e06 e il progetto e07/ (chatc completo, con il
# suo Makefile). Il problema 11.6 ha anche la versione con i difetti:
#   make bug          prova a costruirla: il primo difetto e' un errore di collegamento
#   make asan-bug     dopo aver corretto il primo: con AddressSanitizer
#   make release-bug  -O2 -DNDEBUG: e' la variante in cui si vede il sesto

CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SEMPLICI = bin/e01 bin/e02 bin/e03 bin/e04 bin/e05

E06     = e06.c e06_misure.c e06_testo.c
E06_BUG = e06_bug.c e06_bug_misure.c e06_bug_testo.c

all: $(SEMPLICI) bin/e06
	$(MAKE) -C e07 all

bin/e06: $(E06) e06_misure.h e06_testo.h | bin
	$(CC) $(CFLAGS) -o $@ $(E06) $(LDLIBS)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bug: | bin
	$(CC) $(CFLAGS) -o bin/e06_bug $(E06_BUG) $(LDLIBS)

asan-bug: | bin
	$(CC) $(CFLAGS) -fsanitize=address -o bin/e06_bug $(E06_BUG) $(LDLIBS)

release-bug: | bin
	$(CC) -std=c17 -Wall -Wextra -Werror -O2 -DNDEBUG -o bin/e06_bug $(E06_BUG) $(LDLIBS)

test:
	$(MAKE) -C e07 test

bin:
	mkdir -p bin

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf bin
	$(MAKE) -C e07 clean

.PHONY: all bug asan-bug release-bug test asan clean
