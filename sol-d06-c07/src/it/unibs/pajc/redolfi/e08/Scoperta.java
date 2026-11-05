package it.unibs.pajc.redolfi.e08;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Trovare il server sulla rete locale con UDP: il client grida "WHATSPAJC?" in
 * broadcast sulla porta 1234, il server risponde "WHATSPAJC porta" al mittente.
 */
public class Scoperta implements Runnable {
    public static final int PORTA_UDP = 1234;
    private static final String DOMANDA = "WHATSPAJC?";
    private final int portaChat;

    public Scoperta(int portaChat) { this.portaChat = portaChat; }

    /** Lato server: risponde a ogni domanda, per sempre. */
    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(PORTA_UDP)) {
            byte[] buf = new byte[256];
            while (true) {
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                socket.receive(p);                                 // aspetta un datagramma
                String testo = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);
                if (!testo.equals(DOMANDA)) continue;              // non e' per noi
                byte[] risposta = ("WHATSPAJC " + portaChat).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(risposta, risposta.length, p.getAddress(), p.getPort()));
            }
        } catch (IOException e) {
            System.out.println("scoperta: " + e.getMessage());
        }
    }

    /** Lato client: grida e aspetta al massimo {@code attesaMs}: host e porta del server. */
    public static Optional<String> cerca(int attesaMs) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {        // porta a caso
            socket.setBroadcast(true);
            socket.setSoTimeout(attesaMs);
            byte[] domanda = DOMANDA.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(domanda, domanda.length,
                    InetAddress.getByName("255.255.255.255"), PORTA_UDP));
            byte[] buf = new byte[256];
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            socket.receive(p);                                       // la prima risposta
            String testo = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);
            if (!testo.startsWith("WHATSPAJC ")) return Optional.empty();
            return Optional.of(p.getAddress().getHostAddress() + ":" + testo.substring(10));
        } catch (SocketTimeoutException e) {
            return Optional.empty();                                 // nessuno ha risposto
        }
    }
}
