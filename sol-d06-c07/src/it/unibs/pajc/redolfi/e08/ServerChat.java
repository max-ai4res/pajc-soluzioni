package it.unibs.pajc.redolfi.e08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.8 - WhatsPAJC completa, il server: il protocollo e' la tabella COMANDI di
 * Protocollo (un'Azione per comando: argomenti, aiuto, che cosa fa) con le stanze e
 * /ping; la Sessione la consulta riga per riga, con un metodo per azione, e le risposte
 * sono righe di testo composte dove nascono. Sessioni su thread virtuali con il timeout
 * a scatti, registro, arresto ordinato da console ("stop"), allegati a cornici
 * (porta+1), pagina di stato HTTP (porta+2) e scoperta UDP (porta 1234). Il client e'
 * ClientApp in questo package, con la tabella speculare delle righe del server nel
 * ChatController.
 *
 * Uso: ServerChat [porta]
 */
public class ServerChat {
    private final ServerSocket server;
    private final Registro registro = new Registro();
    private final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

    private final Allegati allegati;
    private final PaginaStato stato;

    public ServerChat(int porta) throws IOException {
        server = new ServerSocket(porta);
        allegati = new Allegati(porta + 1, registro);     // i byte su un secondo socket
        stato = new PaginaStato(porta + 2, registro);     // la pagina per il browser
        Thread.ofVirtual().start(allegati);
        Thread.ofVirtual().start(stato);
        Thread.ofVirtual().start(new Scoperta(porta));    // "WHATSPAJC?" su UDP
    }

    /** Il ciclo di accept: una Sessione per client, su un thread virtuale. */
    public void avvia() {
        System.out.println("WhatsPAJC in ascolto sulla porta " + server.getLocalPort()
                + ", allegati su " + (server.getLocalPort() + 1)
                + ", pagina di stato su http://localhost:" + (server.getLocalPort() + 2) + "/stato; 'stop' per fermare");
        try {
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, registro));
            }
        } catch (IOException e) {
            System.out.println("accept interrotto: " + e.getMessage());   // chiuso da ferma()
        }
        exec.close();                                                     // aspetta le sessioni
        System.out.println("server fermo");
    }

    /** Da un altro thread: niente nuovi client, saluta quelli collegati, chiude. */
    public void ferma() throws IOException {
        server.close();                               // accept() lancia SocketException
        allegati.chiudi();
        stato.chiudi();
        registro.aTutti("CIAO");
        for (String nome : registro.nomi()) {
            Sessione s = registro.trova(nome);
            if (s != null) s.chiudi();                // readLine() lancia: la sessione finisce
        }
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        ServerChat chat = new ServerChat(porta);
        Thread console = new Thread(() -> {           // "stop" dalla tastiera ferma il server
            try (BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in))) {
                String riga;
                while ((riga = tastiera.readLine()) != null)
                    if (riga.strip().equals("stop")) { chat.ferma(); return; }
            } catch (IOException e) { /* fine */ }
        });
        console.setDaemon(true);
        console.start();
        chat.avvia();
    }
}
