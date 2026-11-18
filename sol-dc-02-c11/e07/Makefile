# chatc: il progetto. Sorgenti in src/, header in include/, prove in tests/;
# oggetti in obj/ ed eseguibili in bin/, che si possono cancellare e rifare.

CC       = gcc
CPPFLAGS = -Iinclude -MMD -MP
CFLAGS   = -std=c17 -Wall -Wextra -Werror -g
LDLIBS   =

SORGENTI = $(wildcard src/*.c)
OGGETTI  = $(patsubst src/%.c,obj/%.o,$(SORGENTI))
MODULI   = $(filter-out obj/main.o,$(OGGETTI))
PROVE    = $(patsubst tests/%.c,bin/%,$(wildcard tests/*.c))

all: bin/chatc

bin/chatc: $(OGGETTI) | bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

# un oggetto per ogni sorgente, del programma o di una prova
obj/%.o: src/%.c | obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

obj/%.o: tests/%.c | obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

# una prova e' il suo oggetto piu' tutti i moduli, senza il main del programma
bin/test_%: obj/test_%.o $(MODULI) | bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

test: $(PROVE)
	@for p in $(PROVE); do ./$$p || exit 1; done

obj bin lib:
	mkdir -p $@

# le varianti: si riparte da zero, perche' gli oggetti cambiano
debug:   CFLAGS += -DDEBUG
release: CFLAGS += -O2 -DNDEBUG
asan:    CFLAGS += -fsanitize=address
debug release: clean all
asan: clean all test

# la libreria statica con tutti i moduli
lib/libchat.a: $(MODULI) | lib
	ar rcs $@ $^

clean:
	rm -rf obj bin lib

# le dipendenze dagli header, scritte dal compilatore accanto a ogni oggetto
-include $(wildcard obj/*.d)

.PHONY: all test debug release asan clean
.SECONDARY:
