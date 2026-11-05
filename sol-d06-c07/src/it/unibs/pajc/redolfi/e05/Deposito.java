package it.unibs.pajc.redolfi.e05;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Il deposito dei file in transito, sul secondo socket (porta 1235), solo byte:
 *   METTI id + cornice      (il mittente, dopo CARICA)   -> OK
 *   PRENDI id               (il destinatario, dopo PRENDI) -> cornice (vuota se non c'e')
 * Ogni offerta ha un id, e il deposito la tiene finche' il destinatario non la prende, o
 * per cinque minuti: uno ScheduledExecutorService la scarta allo scadere.
 */
public class Deposito implements Runnable {
    public record Offerta(int id, String da, String a, String nome, long dimensione) {}

    private final ServerSocket server;
    private final ConcurrentHashMap<Integer, Offerta> offerte = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, byte[]> byteDi = new ConcurrentHashMap<>();
    private final AtomicInteger prossimoId = new AtomicInteger(1);
    private final ScheduledExecutorService scadenze = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "scadenze"); t.setDaemon(true); return t;
    });

    public Deposito(int porta) throws IOException { server = new ServerSocket(porta); }

    public Offerta offri(String da, Sessione dest, String nome, long dimensione) {
        Offerta o = new Offerta(prossimoId.getAndIncrement(), da, dest.nome(), nome, dimensione);
        offerte.put(o.id(), o);
        scadenze.schedule(() -> {                            // dopo cinque minuti si scarta
            if (offerte.remove(o.id()) != null) byteDi.remove(o.id());
        }, 5, TimeUnit.MINUTES);
        return o;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket s = server.accept();
                Thread.ofVirtual().start(() -> servi(s));
            }
        } catch (IOException e) {
            System.out.println("deposito: " + e.getMessage());
        }
    }

    private void servi(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            String comando = in.readUTF();
            int id = in.readInt();
            switch (comando) {
                case "METTI" -> {
                    byte[] dati = Cornice.leggi(in);
                    if (offerte.containsKey(id)) byteDi.put(id, dati);
                    out.writeUTF(offerte.containsKey(id) ? "OK" : "ERR offerta scaduta");
                    out.flush();
                }
                case "PRENDI" -> {
                    byte[] dati = byteDi.remove(id);
                    offerte.remove(id);
                    Cornice.scrivi(out, dati == null ? new byte[0] : dati);
                }
                default -> out.writeUTF("ERR comando sconosciuto");
            }
        } catch (IOException e) {
            System.out.println("deposito: connessione persa: " + e.getMessage());
        }
    }

    public void chiudi() throws IOException { server.close(); }
}
