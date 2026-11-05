package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Esercizio 3.6 - la calcolatrice remota con il protocollo come codice.
 *
 * Rispetto alla versione del capitolo 2, il protocollo non e' piu' uno switch sparso nel
 * server: e' una tabella (Protocollo.COMANDI) con una riga per operazione, che dice
 * quanti operandi vuole, come si spiega e che cosa fa. La Sessione spezza la riga in
 * operazione e resto, cerca l'operazione nella tabella, controlla la forma degli
 * argomenti (argomenti) e il loro valore (numeri), e solo allora esegue l'azione, che
 * riceve numeri gia' convertiti. Ogni modo di sbagliare la riga ha il suo ERR, e la
 * sessione non lancia mai; AIUTO e' la tabella letta a voce alta. La Sessione lavora su
 * Reader e Writer, cosi' i test la provano su due stringhe. Il server serve un client
 * alla volta e resta in ascolto.
 *
 * Errore tipico: convertire gli operandi dentro l'azione con Long.parseLong e senza
 * catch: "ADD due 3" chiude la connessione invece di rispondere ERR. La conversione sta
 * in un posto solo, prima che l'azione venga chiamata.
 */
public class ServerCalcolatrice {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        try (ServerSocket server = new ServerSocket(porta)) {
            System.out.println("Calcolatrice remota in ascolto sulla porta " + porta);
            while (true) {
                try (Socket socket = server.accept()) {
                    new Sessione(socket).dialoga();
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }
}
