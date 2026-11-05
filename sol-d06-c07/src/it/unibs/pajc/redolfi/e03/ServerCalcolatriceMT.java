package it.unibs.pajc.redolfi.e03;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.3 - la calcolatrice remota completa: il protocollo come tabella di azioni
 * (Protocollo, con i test JUnit: anche STAT), il server per molti client con una
 * Sessione per connessione e il contatore delle operazioni, il client in finestra
 * (CalcolatriceFinestra) con la tabella speculare delle risposte.
 *
 * Il contatore delle operazioni e' stato condiviso fra tutte le sessioni (Statistiche):
 * LongAdder (molti thread che incrementano, pochi che leggono); i client collegati sono
 * un AtomicInteger aggiornato in entrata e nel finally. STAT risponde con entrambi. Il
 * dialogo e' in Sessione, provato dai test su due stringhe.
 *
 * Uso: ServerCalcolatriceMT [porta]
 *
 * Errore tipico: "static int operazioni; operazioni++" nelle sessioni: incrementi
 * persi, di rado, mai riproducibili a mano.
 */
public class ServerCalcolatriceMT {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        Statistiche statistiche = new Statistiche();
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Calcolatrice remota (molti client) sulla porta " + porta);
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, statistiche));
            }
        }
    }
}
