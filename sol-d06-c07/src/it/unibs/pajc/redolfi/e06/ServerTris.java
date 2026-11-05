package it.unibs.pajc.redolfi.e06;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.6 - il tris in rete completo: arbitro, giocatori e SPETTATORI.
 *
 * Il protocollo e' la tabella COMANDI di Protocollo (un'Azione per comando: argomenti,
 * aiuto, che cosa fa), che la Sessione consulta riga per riga con un metodo per azione;
 * le righe del server sono testo composto dove nasce (Partita, Arbitro, Sessione).
 * /guarda (senza argomento) -> PARTITE a-b,c-d; /guarda Marco-Giulia -> la sessione
 * riceve PARTITA (per sapere i nomi), poi STATO con la griglia corrente e il turno, poi
 * tutte le MOSSA e l'esito, come i giocatori. Lo stato corrente e l'iscrizione fra gli
 * spettatori avvengono sotto il lock della partita (Partita.guarda): nessuna mossa puo'
 * infilarsi fra i due. Le partite in corso stanno in un registro dell'Arbitro
 * (ConcurrentHashMap) e ne escono alla fine.
 *
 * Tre oggetti condivisi, ciascuno con il suo lock: l'Arbitro (la coda dei liberi),
 * la Partita (griglia e turno, condivisa dalle DUE sessioni che la giocano) e ogni
 * Sessione (invia sincronizzato). Una mossa arriva sul thread di chi la fa, entra in
 * Partita.mossa con il lock della partita, e da li' partono gli avvisi a entrambi: due
 * mosse "contemporanee" vengono messe in fila e la seconda riceve "non e' il tuo
 * turno". Chi se ne va a meta' (anche staccando) fa vincere l'altro (ABBANDONO), che
 * torna libero. Finita una partita, i due tornano in coda e, se ci sono, vengono
 * riaccoppiati. Client: ClientTris (console) e TrisFinestra, con la tabella speculare
 * delle righe del server.
 *
 * Uso: ServerTris [porta]
 *
 * Errore tipico: tenere il turno nella sessione invece che nella partita, o non
 * sincronizzare mossa(): due giocatori che muovono nello stesso istante scrivono
 * entrambi sulla griglia.
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
