package it.unibs.pajc.redolfi.e06;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Manda tre cornici (una vuota e' la fine): una con degli a capo dentro arriva intera. */
public class ClientContaByte {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        try (Socket socket = new Socket(host, 1234);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            byte[][] cornici = {
                "ciao".getBytes(StandardCharsets.UTF_8),
                "prima riga\nseconda riga\nterza\n".getBytes(StandardCharsets.UTF_8),   // a capo dentro
                new byte[100_000],
                new byte[0]                                                           // la fine
            };
            for (byte[] c : cornici) {
                Cornice.scrivi(out, c);
                System.out.println(c.length + " byte -> " + in.readUTF());
            }
        }
    }
}
