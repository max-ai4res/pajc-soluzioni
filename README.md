# PAJC · Soluzioni degli esercizi delle dispense

Programmazione Avanzata in Java e C (Università degli Studi di Brescia, Ingegneria
Informatica), prof. Massimiliano Redolfi. Le dispense del corso sono su
<https://redolfi-unibs.github.io>.

Ogni progetto raccoglie le soluzioni dei **problemi** di un capitolo di una dispensa:
`sol-d04-c05` è il capitolo 5 della dispensa 4. Dentro, un package per esercizio,
`it.unibs.pajc.redolfi.e02` per l'Esercizio 5.2, con la classe che ha il `main` e le
altre classi che le servono; le risorse (file di testo, immagini) stanno in `res/e02/`.
Ogni classe principale apre con un commento che dice l'idea della soluzione e l'errore
tipico da evitare. Le risposte brevi degli esercizi "a mente" (prevedere un output,
contare i thread) sono nella dispensa stessa.

Il repository è un workspace Eclipse: si clona (o si scarica lo zip) e si importano i
progetti con *File › Import › Existing Projects into Workspace*. Java 21.

Le soluzioni di un capitolo compaiono qui dopo che gli esercizi sono stati affrontati:
provare prima da soli è l'unico modo in cui servono a qualcosa.

<!-- progetti: tabella scritta da scripts/lab (pajc-new-world), non modificarla a mano -->
| Progetto | Contenuto |
|----------|-----------|
| `sol-d04-c02` | Dispensa 4, cap. 2 (processi, thread e la JVM): i thread di una JVM vuota, mille thread che dormono, tre stati in una fotografia |
| `sol-d04-c03` | Dispensa 4, cap. 3 (il primo thread): contatore di secondi interrotto, avanzamento a puntini con `join(ms)`, `MyTask` educato, quattro thread a mano bilanciati |
| `sol-d04-c04` | Dispensa 4, cap. 4 (executor, `Callable` e `Future`): numeri di Armstrong su tutti i core, il programma che non muore, `Matrix.mapMT`, `invokeAny` fra tre strategie |
| `sol-d04-c05` | Dispensa 4, cap. 5 (quando i thread condividono): `Counter` in tre modi, il deadlock dal vivo, produttore e consumatore con `wait`/`notifyAll`, registro a più scrittori |
| `sol-d04-c06` | Dispensa 4, cap. 6 (`java.util.concurrent`): parole di molti file con `ConcurrentHashMap`, pool di connessioni con `Semaphore`, cache con `ReadWriteLock`, pipeline a tre stadi |
| `sol-d04-c07` | Dispensa 4, cap. 7 (il tempo e l'attesa): periodico lento e il periodico che muore, dieci server lenti con thread virtuali, lo stream parallelo che scrive |
| `sol-d04-c08` | Dispensa 4, cap. 8 (thread e Swing): orologio con `Timer` ed executor, righe che arrivano con `publish`/`process`, Annulla con `SwingWorker`, conteggio dal vivo |
| `sol-d04-c09` | Dispensa 4, cap. 9 (palestra): contare i thread, numeri felici MT, crivello a blocchi, listener, code al supermercato, download con thread virtuali, `PrimeFinderApp` completa, Mandelbrot, `Memo` |
| `sol-d05-c02` | Dispensa 5, cap. 2 (la finestra e i suoi componenti): calcolatrice in finestra, modulo ben allineato, blocco note, pannello di controllo che regge caratteri più grandi |
| `sol-d05-c03` | Dispensa 5, cap. 3 (eventi, ascoltatori e modelli): contatore con un solo ascoltatore, Celsius e Fahrenheit senza rimbalzi, lista della spesa con DefaultListModel, tastierino con EventListenerList |
| `sol-d05-c04` | Dispensa 5, cap. 4 (finestre di dialogo): indovina il numero con JOptionPane, login con Optional, mini editor con apri, salva e conferma, finestra Trova non modale |
| `sol-dxtr-01-c02` | Dispensa extra 1, cap. 2 (la classe come oggetto): la scheda completa di una classe da riga di comando, l'ordine di inizializzazione, `compatibile(o, nomeTipo)`, la fabbrica di forme da un file con `forName` e il costruttore |
| `sol-dxtr-01-c03` | Dispensa extra 1, cap. 3 (campi, metodi, costruttori): un `toString` per tutti, uguali campo per campo, una shell per gli oggetti con `invoke`, copia delle proprietà fra getter e setter |
| `sol-dxtr-01-c04` | Dispensa extra 1, cap. 4 (annotazioni e proxy): l'indice degli autori con `@Repeatable`, il mini-JUnit completo (`@Dopo`, timeout, riga di comando), il validatore dei campi, `Misurato` con un proxy che accumula chiamate e tempi |
| `sol-dxtr-01-c05` | Dispensa extra 1, cap. 5 (class loader, jar e plugin): chi ha caricato chi, un lanciatore con `URLClassLoader`, un'applicazione a comandi con i plugin in `plugin/` (il jar viene da `sol-dxtr-01-c05-plugin`), ricarica a caldo e scarico delle classi |
| `sol-dxtr-01-c05-plugin` | I tre comandi (`data`, `conta`, `inverti`) dell'applicazione dell'Esercizio 5.8: un progetto a parte che dipende da `sol-dxtr-01-c05` per l'interfaccia `Comando` e dichiara le classi in `META-INF/services` |
| `sol-dxtr-01-c06` | Dispensa extra 1, cap. 6 (il compilatore dentro il programma): le diagnostiche di un file con il cursore, la calcolatrice che impara operatori a runtime, un piccolo REPL, il compilatore tutto in memoria misurato contro quello su disco |
| `sol-dxtr-01-c07` | Dispensa extra 1, palestra: prevedere la reflection, la scheda del bimondo, il runner che trova le classi di test in una cartella, la calcolatrice grafica che impara, l'ispettore di finestre, un gestore di plugin generico, **il Plot completo** (espressioni compilate, `plugin/`, Salva come plugin) |
| `sol-d05-c05` | Dispensa 5, cap. 5 (il componente che si disegna): griglia 10×10 che si adatta, quadrante di un orologio, istogramma con le etichette centrate, poligoni e stelle con Polygon |
| `sol-d05-c06` | Dispensa 5, cap. 6 (Graphics2D): tratteggio con BasicStroke, casetta come una sola Area, cerchio cromatico con Arc2D, ingranaggio con Path2D e Area |
| `sol-dxtr-02-c02` | Dispensa extra 2, cap. 2 (il mondo e la sua immagine: MVC): contatore con due viste, semaforo pedonale con il tempo in avanza(dt), gioco della vita su un toro con la generazione ogni 0,2 s misurati, Palline con una seconda vista a console |
| `sol-dxtr-02-c03` | Dispensa extra 2, cap. 3 (il modello che avvisa): termostato con tre proprietà e isteresi, cronometro con tick e giri su EventListenerList, lista della spesa con un ListModel che legge dal modello, registratore degli eventi di Palline su file e riproduzione a passo fisso |
| `sol-d05-c07` | Dispensa 5, cap. 7 (coordinate e trasformazioni): assi con le tacche, campo di forze con rotate, sistema solare con trasformazioni annidate, grafico di due funzioni con zoom |
| `sol-d05-c08` | Dispensa 5, cap. 8 (mouse, tastiera e tempo): cerchio che segue il mouse, figure da trascinare, gioco della vita, planetario animato |
| `sol-d05-c09` | Dispensa 5, palestra: finestre da prevedere, cerchio e assi, convertitore, visualizzatore di immagini, omino articolato, dama, navicella, la Lavagna completa con annulla e ripeti |
| `sol-dxtr-02-c04` | Dispensa extra 2, cap. 4 (un mondo che rimbalza bene): la legge del rimbalzo misurata contro h·e^2n, il piano inclinato come segmento con l'attrito per unità di tempo, il biliardo senza buche con la stecca, il flipper minimo con respingenti e alette che ruotano |
| `sol-dxtr-02-c05` | Dispensa extra 2, cap. 5 (comportamenti, stati e comandi): il semaforo con le fasi come stati, il distributore automatico con stati e comandi sealed, l'editor di figure con annulla e ripeti su due pile, Snake con la coda delle direzioni e gli stati della partita |
| `sol-dxtr-02-c06` | Dispensa extra 2, cap. 6 (creare e condividere): la configurazione come enum singleton e come record passato, la fabbrica di forme da file con il numero di riga, il pool di proiettili della navicella, i temi grafici come Flyweight con il Disegnatore come Strategy |
| `sol-dxtr-02-c07` | Dispensa extra 2, palestra: chi parla con chi, il contatore in tre versioni, Pong, il gioco della vita completo, il biliardo con le buche e Rivedi, Mattoni con i livelli da file, Asteroidi con il pool e l'invulnerabilità a tempo, Palline completa con il salvataggio dello scenario e il grafico dell'energia |
| `sol-d06-c02` | Dispensa 6, cap. 2 (socket e TCP): il server dell'ora, eco con contatore, la calcolatrice remota che non muore mai, il benvenuto a più righe chiuso da una riga vuota |
| `sol-d06-c03` | Dispensa 6, cap. 3 (il protocollo): la calcolatrice con la tabella delle operazioni e JUnit, il quiz a passo guidato dal server, il registro dei messaggi con /storia, un mini-HTTP letto dal browser |
| `sol-d06-c04` | Dispensa 6, cap. 4 (un server per molti): l'ora per molti con il timeout come orologio, le stanze, inattività e /ping, il tris in rete con il server arbitro |
| `sol-d06-c05` | Dispensa 6, cap. 5 (il client con la finestra): l'ora con SwingWorker, la calcolatrice in finestra con la coda delle richieste, WhatsPAJC a schede con i non letti, il tris in finestra |
| `sol-d06-c06` | Dispensa 6, cap. 6 (byte, oggetti e datagrammi): il contatore di byte a cornici, il trasferimento con la barra e Annulla, la connessione automatica con la scoperta UDP, un server HTTP di file |
| `sol-d06-c07` | Dispensa 6, cap. 7 (palestra): il server dell'ora in due versioni misurate, eco a rovescio con scadenza, calcolatrice completa con STAT, quiz a più giocatori, file fra client, tris con gli spettatori, sondaggio in aula, WhatsPAJC completa |
| `sol-dc-01-c02` | Dispensa C 1, cap. 2 (dal sorgente all'eseguibile): saluta gli argomenti, la tabellina in tre file con il Makefile a mano, due programmi in una cartella con un modulo condiviso, i bersagli debug e asan |
| `sol-dc-01-c03` | Dispensa C 1, cap. 3 (funzioni e memoria): dump dei byte di int, short, double e stringhe, la misura del frame e la ricorsione che esaurisce lo stack, per valore contro per puntatore misurati con clock |
| `sol-dc-01-c04` | Dispensa C 1, cap. 4 (puntatori): swap, scambia_e_azzera e swap_ptr, min_max con due risultati per puntatore, dividi con esito e puntatori NULL ammessi, trova e azzera tutte le occorrenze |
| `sol-dc-01-c05` | Dispensa C 1, cap. 5 (array, aritmetica e stringhe): reverse, is_palindrome e freq, la matrice allocata con il parametro VLA, parole e tagli con fgets, la rubrica a dimensione fissa ordinata con memmove |
| `sol-dc-01-c06` | Dispensa C 1, cap. 6 (stack, heap e allocazione dinamica): leggi_interi con malloc, concatena e duplica sullo heap, il vettore dinamico che raddoppia e dimezza, i quattro errori da trovare con ASan (e09_quattro.c) e il diario della correzione |
| `sol-dc-01-c07` | Dispensa C 1, cap. 7 (struct e typedef): Punto e Segmento, il Rectangle del bimondo con le funzioni con prefisso, Studente con la copia profonda, il Poligono con i vertici allocati e l'area di Gauss |
| `sol-dc-01-c08` | Dispensa C 1, cap. 8 (union, campi di bit, padding): i byte di un intero con shift e con union, tre flag in un byte, il Recapito come union discriminata, il dump di una struct con il padding evidenziato |
| `sol-dc-01-c09` | Dispensa C 1, cap. 9 (preprocessore e macro): SQUARE, LOG e SWAP corrette con SWAP_T, una libreria di macro con la guardia, un progetto con debug, release e asan, ASSERT_MSG sulla rubrica dinamica |
| `sol-dc-01-c10` | Dispensa C 1, palestra: prevedere prima di eseguire, la stringa dinamica, la matrice dinamica a moduli, il decodificatore di byte, la pila di rettangoli, il debugging con ASan (e06_bug.c e il diario), la rubrica completa in tre file con Makefile |
<!-- /progetti -->
