package it.unibs.pajc.redolfi.e05;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.5 - il trasferimento di file fra due client passando dal server.
 *
 * Testo e byte su due socket. Sul socket della chat (1234) il protocollo e' la tabella
 * COMANDI di Protocollo, con due comandi in piu' rispetto alla chat: "/invia utente nome
 * dimensione" (il client lo compone da un percorso locale) -> la Sessione crea
 * un'offerta con un id nel Deposito, risponde CARICA id al mittente e manda FILE
 * mittente nome dimensione id al destinatario; il destinatario risponde /accetta ->
 * PRENDI id nome. Sul socket del deposito (1235), a cornici: il mittente fa METTI id +
 * byte, il destinatario PRENDI id e salva in ricevuti/ (solo getFileName, mai
 * sovrascrivere). Il deposito tiene i byte finche' non vengono presi o per cinque minuti
 * (ScheduledExecutorService). Client in finestra: ClientFileFinestra, con la tabella
 * speculare delle righe del server e due barre in due SwingWorker.
 *
 * Uso: ServerChatFile [porta]   (il deposito e' su porta+1)
 *
 * Errore tipico: il nome del file preso cosi' com'e' dal mittente ("../../.bashrc").
 */
public class ServerChatFile {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        Registro registro = new Registro();
        Deposito deposito = new Deposito(porta + 1);
        Thread.ofVirtual().start(deposito);
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("WhatsPAJC con i file: chat " + porta + ", deposito " + (porta + 1));
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, registro, deposito));
            }
        }
    }
}
