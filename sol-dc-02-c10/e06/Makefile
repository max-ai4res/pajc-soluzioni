# Esercizio 10.6: un Makefile che non nomina nessun sorgente.
CC       = gcc
CPPFLAGS = -Iinclude -MMD -MP
CFLAGS   = -std=c17 -Wall -Wextra -Werror -g
LDLIBS   =

SORGENTI = $(wildcard src/*.c)
OGGETTI  = $(patsubst src/%.c,obj/%.o,$(SORGENTI))

all: bin/rubrica

bin/rubrica: $(OGGETTI) | bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

obj/%.o: src/%.c | obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

obj bin:
	mkdir -p $@

# toccato un header, si ricompila solo chi lo include
verifica: all
	sleep 1                 # make confronta le date al secondo
	touch include/rubrica.h
	$(MAKE) all

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf obj bin

-include $(wildcard obj/*.d)

.PHONY: all verifica asan clean
