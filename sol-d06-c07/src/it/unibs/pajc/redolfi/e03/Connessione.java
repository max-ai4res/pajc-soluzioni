package it.unibs.pajc.redolfi.e03;

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
 * La connessione dietro la finestra, come nella dispensa: thread lettore e consegna
 * delle righe sull'EDT con invokeLater; chiusa(motivo) alla fine (null se normale).
 */
public class Connessione implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;

    public Connessione(String host, int porta, Consumer<String> ricevi, Consumer<String> chiusa)
            throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, porta), 3000);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
        Thread.ofVirtual().start(() -> {
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
    }

    /** Manda una riga: corta, dall'EDT va bene. */
    public void invia(String riga) { out.println(riga); }

    @Override
    public void close() {
        try { socket.close(); } catch (IOException e) { /* gia' chiuso */ }
    }
}
