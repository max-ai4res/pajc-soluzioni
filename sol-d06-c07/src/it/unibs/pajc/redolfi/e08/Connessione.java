package it.unibs.pajc.redolfi.e08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

/**
 * La connessione dietro la finestra: apre il socket, avvia il thread lettore e consegna
 * ogni riga al Consumer SUL THREAD DEGLI EVENTI (invokeLater). Chi la usa non vede
 * mai socket, stream o thread: riceve righe e manda righe.
 */
public class Connessione implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final Thread lettore;

    /**
     * Apre la connessione (con un timeout) e avvia il lettore. Le righe arrivano a
     * {@code ricevi} sull'EDT; alla fine della connessione {@code chiusa} viene eseguito,
     * sempre sull'EDT, con il motivo (null se chiusura normale).
     */
    public Connessione(String host, int porta, Consumer<String> ricevi,
                       Consumer<String> chiusa) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, porta), 3000);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
        lettore = Thread.ofVirtual().unstarted(() -> {
            String motivo = null;
            try {
                in.lines().forEach(riga ->                             // fino a null
                        SwingUtilities.invokeLater(() -> ricevi.accept(riga)));
            } catch (UncheckedIOException e) {
                motivo = socket.isClosed() ? null : e.getCause().getMessage();
            }
            String m = motivo;
            SwingUtilities.invokeLater(() -> chiusa.accept(m));
        });
        lettore.start();
    }

    /** Manda una riga: corta, dall'EDT va bene. */
    public void invia(String riga) {
        out.println(riga);
    }

    @Override
    public void close() {
        try {
            socket.close();                 // il lettore esce dal readLine
        } catch (IOException e) {
            // gia' chiuso
        }
    }
}
