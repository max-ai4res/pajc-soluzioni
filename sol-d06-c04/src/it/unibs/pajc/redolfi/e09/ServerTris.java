package it.unibs.pajc.redolfi.e09;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esercizio 4.9 - il tris in rete: server arbitro, due giocatori per partita.
 *
 * Il protocollo e' la tabella COMANDI di Protocollo: /nome N, /m R C, /esci, /aiuto;
 * una riga della tabella dice quanti argomenti vuole e quale azione della Sessione
 * eseguire, e i controlli di valore (nomeValido, coordinata da 0 a 2) stanno accanto
 * alla tabella. Dal server: PARTITA X Giulia e TOCCA all'inizio, MOSSA X 1 2 a
 * entrambi dopo ogni mossa valida, VINCE X o PATTA alla fine, ABBANDONO N se l'altro se
 * ne va, ERR se la mossa non e' lecita. Il client di console (ClientTris) ha la tabella
 * speculare, da parola del server ad azione sullo schermo.
 *
 * Tre oggetti condivisi, ciascuno con il suo lock: l'Arbitro (la coda dei liberi),
 * la Partita (griglia e turno, condivisa dalle DUE sessioni che la giocano) e ogni
 * Sessione (invia sincronizzato). Una mossa arriva sul thread di chi la fa, entra in
 * Partita.mossa con il lock della partita, e da li' partono gli avvisi a entrambi: due
 * mosse "contemporanee" vengono messe in fila e la seconda riceve "non e' il tuo
 * turno". Chi se ne va a meta' (anche staccando) fa vincere l'altro (ABBANDONO), che
 * torna libero. Finita una partita, i due tornano in coda e, se ci sono, vengono
 * riaccoppiati.
 *
 * Errore tipico: tenere il turno nella sessione invece che nella partita, o non
 * sincronizzare mossa(): due giocatori che muovono nello stesso istante scrivono
 * entrambi sulla griglia.
 *
 * Si prova con due ClientTris o con due nc localhost 1234 (la porta si puo' passare
 * come argomento).
 */
public class ServerTris {

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : Protocollo.PORTA;
        Arbitro arbitro = new Arbitro();
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Tris in rete sulla porta " + porta);
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, arbitro));
            }
        }
    }
}
