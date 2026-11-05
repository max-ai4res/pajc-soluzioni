package it.unibs.pajc.redolfi.e04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Palestra 7.4 - il quiz a piu' giocatori: la stessa domanda a tutti, il primo che
 * risponde giusto prende il punto (PUNTO nome), venti secondi e poi TEMPO, classifica
 * con /classifica, "via" dalla console del server per cominciare, FINE con la
 * classifica finale. Il protocollo e' la tabella COMANDI di Protocollo (un'Azione per
 * comando: argomenti, aiuto, che cosa fa); la Sessione la consulta riga per riga, con un
 * metodo per azione, e le risposte sono righe di testo composte dove nascono. Lo stato
 * sta in Quiz, con i metodi synchronized; le sessioni e il timer
 * (ScheduledExecutorService) lo chiamano; chi entra a quiz iniziato riceve la domanda
 * corrente. Client: ClientQuizMulti, con la tabella speculare delle righe del server.
 *
 * Uso: ServerQuizMulti [file-domande] [porta]
 *
 * Errore tipico: "due primi": controllare che la domanda sia aperta e assegnare il punto
 * in due passi non sincronizzati; con due risposte nello stesso istante entrambi
 * vincono.
 */
public class ServerQuizMulti {

    public static void main(String[] args) throws IOException {
        List<Quiz.Quesito> quesiti = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "res/e04/domande.txt"),
                StandardCharsets.UTF_8).stream().filter(l -> l.contains("|"))
                .map(l -> new Quiz.Quesito(l.substring(0, l.indexOf('|')).strip(), l.substring(l.indexOf('|') + 1).strip()))
                .toList();
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : Protocollo.PORTA;
        Quiz quiz = new Quiz(quesiti);
        Thread console = new Thread(() -> {
            try (BufferedReader t = new BufferedReader(new InputStreamReader(System.in))) {
                String riga;
                while ((riga = t.readLine()) != null) if (riga.strip().equals("via")) quiz.via();
            } catch (IOException e) { /* fine */ }
        });
        console.setDaemon(true);
        console.start();
        try (ServerSocket server = new ServerSocket(porta);
             ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Quiz a piu' giocatori sulla porta " + porta + ": scrivi 'via' per cominciare");
            while (true) {
                Socket socket = server.accept();
                exec.submit(new Sessione(socket, quiz));
            }
        }
    }
}
