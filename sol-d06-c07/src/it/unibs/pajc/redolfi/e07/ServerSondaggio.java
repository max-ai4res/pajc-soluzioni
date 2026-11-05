package it.unibs.pajc.redolfi.e07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Il server del sondaggio: le sessioni degli studenti (TCP) e la scoperta (UDP). Vive
 * nel processo del docente: DocenteApp lo avvia e guarda lo stesso Sondaggio.
 */
public class ServerSondaggio implements Runnable {
    private final Sondaggio sondaggio;
    private final ServerSocket server;

    public ServerSondaggio(Sondaggio sondaggio) throws IOException {
        this.sondaggio = sondaggio;
        server = new ServerSocket(Protocollo.PORTA);
        Thread.ofVirtual().start(this::scoperta);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket s = server.accept();
                Thread.ofVirtual().start(() -> servi(s));
            }
        } catch (IOException e) {
            System.out.println("server fermo: " + e.getMessage());
        }
    }

    private void servi(Socket socket) {
        Consumer<String> ascoltatore = null;
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            Object lock = new Object();
            ascoltatore = riga -> { synchronized (lock) { out.println(riga); } };
            String nome = null;
            String riga;
            while ((riga = in.readLine()) != null) {
                String[] c = riga.strip().split(" ", 2);
                synchronized (lock) {
                    if (c[0].equals("/nome") && nome == null && c.length == 2 && c[1].matches("[A-Za-z0-9_]{2,16}")) {
                        nome = c[1];
                        out.println("BENVENUTO " + nome);
                        for (String r : sondaggio.corrente()) out.println(r);
                        sondaggio.ascolta(ascoltatore);
                    } else if (c[0].equals("/voto") && nome != null && c.length == 2) {
                        try {
                            out.println(sondaggio.vota(nome, Integer.parseInt(c[1].strip())));
                        } catch (NumberFormatException e) {
                            out.println("ERR voto non valido");
                        }
                    } else {
                        out.println(nome == null ? "ERR prima /nome N" : "ERR comando sconosciuto");
                    }
                }
            }
        } catch (IOException e) {
            // il client se n'e' andato
        } finally {
            if (ascoltatore != null) sondaggio.dimentica(ascoltatore);
        }
    }

    private void scoperta() {
        try (DatagramSocket socket = new DatagramSocket(Protocollo.PORTA)) {
            byte[] buf = new byte[64];
            while (true) {
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                socket.receive(p);
                if (!new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8).equals("SONDAGGIO?")) continue;
                byte[] r = ("SONDAGGIO " + Protocollo.PORTA).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(r, r.length, p.getAddress(), p.getPort()));
            }
        } catch (IOException e) {
            System.out.println("scoperta ferma: " + e.getMessage());
        }
    }

    public void chiudi() throws IOException { server.close(); }
}
