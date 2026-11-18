# Esercizio 10.9: una cartella per variante, cosi' cambiare variante non ricompila tutto.
VARIANTE ?= debug
VARIANTI  = debug release asan

ifeq ($(filter $(VARIANTE),$(VARIANTI)),)
$(error variante sconosciuta: "$(VARIANTE)" (scegliere fra: $(VARIANTI)))
endif

CC       = gcc
CPPFLAGS = -Iinclude -MMD -MP
CFLAGS_comuni  = -std=c17 -Wall -Wextra -Werror
CFLAGS_debug   = -g -DDEBUG
CFLAGS_release = -O2 -DNDEBUG
CFLAGS_asan    = -g -fsanitize=address
CFLAGS         = $(CFLAGS_comuni) $(CFLAGS_$(VARIANTE))
# le prove tengono sempre gli assert accesi
CFLAGS_PROVE   = $(filter-out -DNDEBUG,$(CFLAGS))
LDLIBS   =

B = build/$(VARIANTE)

SORGENTI = $(wildcard src/*.c)
OGGETTI  = $(patsubst src/%.c,$(B)/obj/%.o,$(SORGENTI))
MODULI   = $(filter-out $(B)/obj/main.o,$(OGGETTI))
PROVE    = $(patsubst tests/%.c,$(B)/bin/%,$(wildcard tests/*.c))

all: $(B)/bin/rubrica

$(B)/bin/rubrica: $(OGGETTI) | $(B)/bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

$(B)/obj/%.o: src/%.c | $(B)/obj
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $< -o $@

$(B)/obj/%.o: tests/%.c | $(B)/obj
	$(CC) $(CPPFLAGS) $(CFLAGS_PROVE) -c $< -o $@

$(B)/bin/test_%: $(B)/obj/test_%.o $(MODULI) | $(B)/bin
	$(CC) $(CFLAGS) $^ -o $@ $(LDLIBS)

test: $(PROVE)
	@for p in $(PROVE); do ./$$p || exit 1; done

$(B)/obj $(B)/bin:
	mkdir -p $@

# tutte le varianti, ciascuna nella sua cartella
tutte:
	@for v in $(VARIANTI); do $(MAKE) VARIANTE=$$v all test || exit 1; done

clean:
	rm -rf build

-include $(wildcard $(B)/obj/*.d)

.PHONY: all test tutte clean
.SECONDARY:
