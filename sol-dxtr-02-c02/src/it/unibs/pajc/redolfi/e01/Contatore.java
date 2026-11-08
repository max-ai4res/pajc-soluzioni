package it.unibs.pajc.redolfi.e01;

/** Il modello: un numero fra 0 e 100 e le tre operazioni che lo cambiano. Niente Swing. */
public class Contatore {

    public static final int MIN = 0, MAX = 100;

    private int valore;

    public int valore() {
        return valore;
    }

    public void incrementa() {
        if (valore < MAX)
            valore++;
    }

    public void decrementa() {
        if (valore > MIN)
            valore--;
    }

    public void azzera() {
        valore = MIN;
    }
}
