# Esercizio 10.8: il modulo errore, il programma che lo stampa e la sua prova.
CC       = gcc
CPPFLAGS = -Iinclude -MMD -MP
CFLAGS   = -std=c17 -Wall -Wextra -Werror -g
LDLIBS   =

SORGENTI = $(wildcard src/*.c)
OGGETTI  = $(patsubst src/%.c,obj/%.o,$(SORGENTI))
MODULI   = $(filter-out obj/main.o,$(OGGETTI))
PROVE    = $(patsubst tests/%.c,bin/%,$(wildcard tests/*.c))

all: bin/errori

bin/errori: $(OGGETTI) | bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

obj/%.o: src/%.c | obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

obj/%.o: tests/%.c | obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

# una prova: il suo oggetto e i moduli, senza il main del programma
bin/test_%: obj/test_%.o $(MODULI) | bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

# le costruisce tutte e le esegue, fermandosi alla prima che fallisce
test: $(PROVE)
	@for p in $(PROVE); do ./$$p || exit 1; done


obj bin:
	mkdir -p $@

asan: CFLAGS += -fsanitize=address
asan: clean all test

clean:
	rm -rf obj bin

-include $(wildcard obj/*.d)

.PHONY: all test asan clean
.SECONDARY:
