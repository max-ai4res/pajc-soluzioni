package it.unibs.pajc.redolfi.e07;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 4.7 - WhatsPAJC con le stanze.
 *
 * Il protocollo e' la tabella COMANDI di Protocollo con due righe in piu', /entra S e
 * /stanze, e due azioni in piu' nella Sessione, entra e stanze; dal server STANZA s
 * (conferma) e STANZE generale:3,giochi:1. All'ingresso tutti sono in "generale". /chi,
 * i messaggi senza barra, ENTRA ed ESCE riguardano la propria stanza; /a attraversa le
 * stanze. Il nome di stanza ha il suo controllo di valore (stanzaValida: minuscole,
 * cifre e trattino) perche' finisce nella riga STANZE separato da : e da ,.
 *
 * La stanza e' un campo (volatile) della sessione, e il registro la legge scorrendo le
 * sessioni: nessuna seconda struttura da tenere allineata, e una stanza vuota scompare
 * da sola. Cambiare stanza: ESCE alla vecchia, scrittura del campo, STANZA a se', ENTRA
 * alla nuova. Un messaggio alla stanza che passa proprio in quel momento puo' includere
 * o no chi si sta spostando: finestra accettata.
 *
 * Errore tipico: una mappa stanza -> Set<Sessione> aggiornata in due passi senza lock:
 * un utente in due stanze o in nessuna, e una stanza vuota che resta nell'elenco.
 *
 * Si prova con nc localhost 1234 (la porta si puo' passare come argomento).
 */
public class ServerChatStanze {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        Registro registro = new Registro();
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("WhatsPAJC con le stanze sulla porta " + porta);
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, registro));
            }
        }
    }
}
