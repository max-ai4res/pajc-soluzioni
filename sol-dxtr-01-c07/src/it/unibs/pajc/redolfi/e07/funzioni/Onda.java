package it.unibs.pajc.redolfi.e07.funzioni;

import it.unibs.pajc.redolfi.e07.Funzione;
import it.unibs.pajc.redolfi.e07.Plot;

@Plot(nome = "onda quadra (3 armoniche)", xMin = -6.3, xMax = 6.3)
public class Onda implements Funzione {
    public double f(double x) {
        return Math.sin(x) + Math.sin(3 * x) / 3 + Math.sin(5 * x) / 5;
    }
}
