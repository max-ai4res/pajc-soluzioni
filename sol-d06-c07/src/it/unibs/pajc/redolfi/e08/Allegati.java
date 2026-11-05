package it.unibs.pajc.redolfi.e08;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Il server degli allegati: un secondo ServerSocket, solo byte. Un client si collega,
 * dice che cosa vuole (writeUTF) e poi parla a cornici:
 *   METTI nome-utente nome-file  + cornice  -> OK     (il server la tiene e avvisa la chat)
 *   PRENDI nome-file                        -> cornice (vuota se non c'e')
 */
public class Allegati implements Runnable {
    private final ServerSocket server;
    private final Registro registro;
    private final ConcurrentHashMap<String, byte[]> foto = new ConcurrentHashMap<>();

    public Allegati(int porta, Registro registro) throws IOException {
        server = new ServerSocket(porta);
        this.registro = registro;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket s = server.accept();
                Thread.ofVirtual().start(() -> servi(s));
            }
        } catch (IOException e) {
            System.out.println("allegati: " + e.getMessage());
        }
    }

    private void servi(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            String comando = in.readUTF();
            switch (comando) {
                case "METTI" -> {
                    String utente = in.readUTF();
                    String nome = Path.of(in.readUTF()).getFileName().toString();   // niente cartelle
                    byte[] dati = Cornice.leggi(in);
                    foto.put(nome, dati);
                    out.writeUTF("OK");
                    out.flush();
                    registro.aTutti("FOTO " + utente + " " + nome);
                    System.out.printf("[allegati] %s ha messo %s (%d byte)%n", utente, nome, dati.length);
                }
                case "PRENDI" -> {
                    byte[] dati = foto.getOrDefault(in.readUTF(), new byte[0]);
                    Cornice.scrivi(out, dati);
                }
                default -> out.writeUTF("ERR comando sconosciuto");
            }
        } catch (IOException e) {
            System.out.println("[allegati] connessione persa: " + e.getMessage());
        }
    }

    public void chiudi() throws IOException { server.close(); }

    /** Lato client: manda una foto. Un socket nuovo, solo byte, chiuso alla fine. */
    public static void metti(String host, int porta, String utente, Path file) throws IOException {
        try (Socket s = new Socket(host, porta);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
             DataInputStream in = new DataInputStream(s.getInputStream())) {
            out.writeUTF("METTI");
            out.writeUTF(utente);
            out.writeUTF(file.getFileName().toString());
            Cornice.scrivi(out, Files.readAllBytes(file));
            String esito = in.readUTF();
            if (!esito.equals("OK")) throw new IOException(esito);
        }
    }

    /** Lato client: prende una foto. */
    public static byte[] prendi(String host, int porta, String nome) throws IOException {
        try (Socket s = new Socket(host, porta);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {
            out.writeUTF("PRENDI");
            out.writeUTF(nome);
            out.flush();
            return Cornice.leggi(in);
        }
    }
}
