package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 4.8 - WhatsPAJC con il controllo dell'inattivita' e /ping.
 *
 * Il silenzio si misura con setSoTimeout(60 s): ogni volta che readLine scade senza una
 * riga, la sessione conta uno "scaduto". Al secondo (due minuti) manda "INFO sei ancora
 * li'?", al terzo (tre minuti) CIAO e chiude. Qualunque riga ricevuta azzera il
 * contatore. Il ciclo di lettura sta in Sessione.dialoga, con il catch della
 * SocketTimeoutException DENTRO il while. Il protocollo e' la tabella COMANDI di
 * Protocollo con una riga in piu', /ping, e un'azione in piu' nella Sessione, ping, che
 * risponde PONG e non manda niente agli altri: /ping funziona anche prima del nome,
 * perche' l'azione non lo chiede.
 *
 * Errore tipico: catturare SocketTimeoutException fuori dal while: al primo timeout la
 * sessione esce dal ciclo e il client viene scollegato subito, senza avviso.
 *
 * Argomenti facoltativi: la porta e il timeout in millisecondi (per provarlo senza
 * aspettare tre minuti: 1234 2000).
 */
public class ServerChatPing {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        int timeoutMs = args.length > 1 ? Integer.parseInt(args[1]) : Sessione.TIMEOUT_MS;
        Registro registro = new Registro();
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("WhatsPAJC con /ping sulla porta " + porta
                    + " (timeout " + timeoutMs + " ms, avviso al 2o, uscita al 3o)");
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, registro, timeoutMs));
            }
        }
    }
}
