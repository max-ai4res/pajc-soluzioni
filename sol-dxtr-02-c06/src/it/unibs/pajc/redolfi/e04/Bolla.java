package it.unibs.pajc.redolfi.e04;

/** Una bolla di sapone: leggerissima, rimbalza quasi tutto, ma scoppia a un urto forte. */
public record Bolla() implements Materiale {

    public double restituzione() { return 0.95; }
    public double densita()      { return 0.05; }

    @Override
    public boolean sopravvive(double impatto) {
        return impatto < 2.5;                            // m/s
    }
}
