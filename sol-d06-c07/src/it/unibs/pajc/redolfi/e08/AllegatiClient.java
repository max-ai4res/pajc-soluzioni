package it.unibs.pajc.redolfi.e08;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/** Il lato client degli allegati: METTI e PRENDI a cornici su un socket nuovo per ogni foto. */
public final class AllegatiClient {
    private AllegatiClient() {}

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
