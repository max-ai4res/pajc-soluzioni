package it.unibs.pajc.redolfi.e02;

/** Dieci secondi a passi di 16 ms: valore 10, dieci annunci per ciascun canale. */
public class ProvaContatore {

    public static void main(String[] args) {
        Contatore c = new Contatore();
        int[] aMano = { 0 }, proprieta = { 0 };
        c.addContatoreListener(v -> aMano[0]++);
        c.addPropertyChangeListener(e -> proprieta[0]++);
        for (int i = 0; i < 625; i++)
            c.stepNext(0.016);
        if (c.valore() != 10 || aMano[0] != 10 || proprieta[0] != 10)
            throw new AssertionError("valore " + c.valore() + ", annunci " + aMano[0] + " e " + proprieta[0]);
        System.out.println("Contatore: valore 10, dieci annunci per canale, ok");
    }
}
