# Soluzioni dei problemi del capitolo 10 di DC-02 "Funzioni, memoria e moduli"
#
# Ogni problema e' un progetto in una sottocartella (e06/ ... e09/), con il suo
# Makefile. `make` li costruisce tutti; `make test` esegue le prove di quelli
# che ne hanno; `make clean` toglie cio' che e' stato generato.

PROGETTI = e06 e07 e08 e09

all:
	@for p in $(PROGETTI); do $(MAKE) -C $$p all || exit 1; done

test:
	@for p in e07 e08 e09; do $(MAKE) -C $$p test || exit 1; done

clean:
	@for p in $(PROGETTI); do $(MAKE) -C $$p clean; done

.PHONY: all test clean
