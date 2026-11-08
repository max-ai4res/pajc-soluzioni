package it.unibs.pajc.redolfi.e01;

/** Il modello provato senza finestra: i limiti e l'azzeramento. */
public class ProvaContatore {

    static void verifica(boolean condizione, String messaggio) {
        if (!condizione)
            throw new AssertionError(messaggio);
    }

    public static void main(String[] args) {
        Contatore c = new Contatore();
        verifica(c.valore() == 0, "parte da zero");
        c.decrementa();
        verifica(c.valore() == 0, "non scende sotto zero");
        for (int i = 0; i < 150; i++)
            c.incrementa();
        verifica(c.valore() == 100, "non sale sopra cento");
        c.decrementa();
        verifica(c.valore() == 99, "decrementa");
        c.azzera();
        verifica(c.valore() == 0, "azzera");
        System.out.println("Contatore: tutto ok");
    }
}
