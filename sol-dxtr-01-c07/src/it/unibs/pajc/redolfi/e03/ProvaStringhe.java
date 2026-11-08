package it.unibs.pajc.redolfi.e03;

/** Una seconda classe di test, con un blocco statico che non deve girare prima del tempo. */
public class ProvaStringhe {
    static { System.out.println("  [ProvaStringhe inizializzata: solo quando si esegue]"); }

    @Test public void lunghezza() { if ("reflection".length() != 10) throw new AssertionError(); }
    @Test public void maiuscole() { if (!"abc".toUpperCase().equals("ABC")) throw new AssertionError(); }
}
