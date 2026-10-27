package it.unibs.pajc.redolfi.e08;

/** Le forme che la Lavagna sa disegnare. */
public enum Forma {
    RETTANGOLO("Rettangolo"), ELLISSE("Ellisse"), MANO_LIBERA("Mano libera");

    private final String etichetta;

    Forma(String etichetta) { this.etichetta = etichetta; }

    public String etichetta() { return etichetta; }
}
