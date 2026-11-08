package it.unibs.pajc.redolfi.e04;

public record Gomma() implements Materiale {

    public double restituzione() { return 0.8; }
    public double densita()      { return 1.0; }
}
