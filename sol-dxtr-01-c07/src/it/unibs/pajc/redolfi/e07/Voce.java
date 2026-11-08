package it.unibs.pajc.redolfi.e07;

/** Una voce del catalogo: la funzione e come si presenta. */
public record Voce(String nome, Funzione f, double xMin, double xMax) {
    @Override
    public String toString() {
        return nome;
    }
}
