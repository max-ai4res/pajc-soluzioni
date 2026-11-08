package it.unibs.pajc.redolfi.e07.funzioni;

import it.unibs.pajc.redolfi.e07.Funzione;
import it.unibs.pajc.redolfi.e07.Plot;

@Plot(nome = "Gaussiana", xMin = -4, xMax = 4)
public class Gaussiana implements Funzione {
    public double f(double x) {
        return Math.exp(-x * x / 2);
    }
}
