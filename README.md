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
| `sol-d06-c02` | Dispensa 6, cap. 2 (la classe come oggetto): la scheda completa di una classe da riga di comando, l'ordine di inizializzazione, `compatibile(o, nomeTipo)`, la fabbrica di forme da un file con `forName` e il costruttore |
| `sol-d06-c03` | Dispensa 6, cap. 3 (campi, metodi, costruttori): un `toString` per tutti, uguali campo per campo, una shell per gli oggetti con `invoke`, copia delle proprietà fra getter e setter |
| `sol-d06-c04` | Dispensa 6, cap. 4 (annotazioni e proxy): l'indice degli autori con `@Repeatable`, il mini-JUnit completo (`@Dopo`, timeout, riga di comando), il validatore dei campi, `Misurato` con un proxy che accumula chiamate e tempi |
| `sol-d06-c05` | Dispensa 6, cap. 5 (class loader, jar e plugin): chi ha caricato chi, un lanciatore con `URLClassLoader`, un'applicazione a comandi con i plugin in `plugin/` (il jar viene da `sol-d06-c05-plugin`), ricarica a caldo e scarico delle classi |
| `sol-d06-c05-plugin` | I tre comandi (`data`, `conta`, `inverti`) dell'applicazione dell'Esercizio 5.8: un progetto a parte che dipende da `sol-d06-c05` per l'interfaccia `Comando` e dichiara le classi in `META-INF/services` |
| `sol-d06-c06` | Dispensa 6, cap. 6 (il compilatore dentro il programma): le diagnostiche di un file con il cursore, la calcolatrice che impara operatori a runtime, un piccolo REPL, il compilatore tutto in memoria misurato contro quello su disco |
| `sol-d06-c07` | Dispensa 6, palestra: prevedere la reflection, la scheda del bimondo, il runner che trova le classi di test in una cartella, la calcolatrice grafica che impara, l'ispettore di finestre, un gestore di plugin generico, **il Plot completo** (espressioni compilate, `plugin/`, Salva come plugin) |
| `sol-d05-c05` | Dispensa 5, cap. 5 (il componente che si disegna): griglia 10×10 che si adatta, quadrante di un orologio, istogramma con le etichette centrate, poligoni e stelle con Polygon |
| `sol-d05-c06` | Dispensa 5, cap. 6 (Graphics2D): tratteggio con BasicStroke, casetta come una sola Area, cerchio cromatico con Arc2D, ingranaggio con Path2D e Area |
| `sol-d07-c02` | Dispensa 7, cap. 2 (il mondo e la sua immagine: MVC): contatore con due viste, semaforo pedonale con il tempo in avanza(dt), gioco della vita su un toro con la generazione ogni 0,2 s misurati, Palline con una seconda vista a console |
| `sol-d07-c03` | Dispensa 7, cap. 3 (il modello che avvisa): termostato con tre proprietà e isteresi, cronometro con tick e giri su EventListenerList, lista della spesa con un ListModel che legge dal modello, registratore degli eventi di Palline su file e riproduzione a passo fisso |
| `sol-d05-c07` | Dispensa 5, cap. 7 (coordinate e trasformazioni): assi con le tacche, campo di forze con rotate, sistema solare con trasformazioni annidate, grafico di due funzioni con zoom |
| `sol-d05-c08` | Dispensa 5, cap. 8 (mouse, tastiera e tempo): cerchio che segue il mouse, figure da trascinare, gioco della vita, planetario animato |
| `sol-d05-c09` | Dispensa 5, palestra: finestre da prevedere, cerchio e assi, convertitore, visualizzatore di immagini, omino articolato, dama, navicella, la Lavagna completa con annulla e ripeti |
| `sol-d07-c04` | Dispensa 7, cap. 4 (un mondo che rimbalza bene): la legge del rimbalzo misurata contro h·e^2n, il piano inclinato come segmento con l'attrito per unità di tempo, il biliardo senza buche con la stecca, il flipper minimo con respingenti e alette che ruotano |
| `sol-d07-c05` | Dispensa 7, cap. 5 (comportamenti, stati e comandi): il semaforo con le fasi come stati, il distributore automatico con stati e comandi sealed, l'editor di figure con annulla e ripeti su due pile, Snake con la coda delle direzioni e gli stati della partita |
| `sol-d07-c06` | Dispensa 7, cap. 6 (creare e condividere): la configurazione come enum singleton e come record passato, la fabbrica di forme da file con il numero di riga, il pool di proiettili della navicella, i temi grafici come Flyweight con il Disegnatore come Strategy |
| `sol-d07-c07` | Dispensa 7, palestra: chi parla con chi, il contatore in tre versioni, Pong, il gioco della vita completo, il biliardo con le buche e Rivedi, Mattoni con i livelli da file, Asteroidi con il pool e l'invulnerabilità a tempo, Palline completa con il salvataggio dello scenario e il grafico dell'energia |
<!-- /progetti -->
