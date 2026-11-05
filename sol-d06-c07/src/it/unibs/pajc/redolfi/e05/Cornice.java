package it.unibs.pajc.redolfi.e05;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Una cornice binaria: la lunghezza davanti (4 byte), poi i byte. */
public final class Cornice {
    public static final int MAX = 20 * 1024 * 1024;        // 20 MB: oltre, e' un errore

    private Cornice() {}

    public static void scrivi(DataOutputStream out, byte[] dati) throws IOException {
        out.writeInt(dati.length);
        out.write(dati);
        out.flush();
    }

    public static byte[] leggi(DataInputStream in) throws IOException {
        int n = in.readInt();
        if (n < 0 || n > MAX) throw new IOException("cornice non valida: " + n + " byte");
        byte[] dati = new byte[n];
        in.readFully(dati);                       // tutti gli n byte, in piu' letture se serve
        return dati;
    }
}
