package it.unibs.pajc.redolfi.e08;

public record Piombo() implements Materiale {

    public double restituzione() { return 0.15; }
    public double densita()      { return 11.0; }
}
