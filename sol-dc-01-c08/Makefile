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
