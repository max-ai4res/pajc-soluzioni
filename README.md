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
<!-- /progetti -->
