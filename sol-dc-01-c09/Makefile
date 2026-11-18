CC      = gcc
CFLAGS  = -std=c17 -Wall -Wextra -Werror -g
LDLIBS  = -lm
SRC     = $(wildcard e0?.c)
BIN     = $(patsubst %.c,bin/%,$(SRC))
E08_SRC = e08.c e08_calc.c

all: $(BIN)

bin/e07: e07.c e07_macro.h | bin
	$(CC) $(CFLAGS) -o $@ e07.c $(LDLIBS)

bin/e08: $(E08_SRC) e08_calc.h e08_util.h | bin
	$(CC) $(CFLAGS) -o $@ $(E08_SRC) $(LDLIBS)

bin/%: %.c | bin
	$(CC) $(CFLAGS) -o $@ $< $(LDLIBS)

bin:
	mkdir -p bin

# e06: ricompila con -DDEBUG, cosi' il LOG compare
debug: CFLAGS += -DDEBUG
debug: | bin
	$(CC) $(CFLAGS) -o bin/e06 e06.c $(LDLIBS)

# e08: i tre eseguibili (il terzo, asan, e' il bersaglio generale qui sotto)
debug-e08: CFLAGS += -DDEBUG -O0
debug-e08: | bin
	$(CC) $(CFLAGS) -o bin/e08 $(E08_SRC) $(LDLIBS)

release-e08: CFLAGS += -O2 -DNDEBUG
release-e08: | bin
	$(CC) $(CFLAGS) -o bin/e08 $(E08_SRC) $(LDLIBS)

asan: CFLAGS += -fsanitize=address
asan: clean all

clean:
	rm -rf bin

.PHONY: all asan clean debug debug-e08 release-e08
