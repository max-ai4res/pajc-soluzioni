package it.unibs.pajc.redolfi.e07.funzioni;

import it.unibs.pajc.redolfi.e07.Funzione;
import it.unibs.pajc.redolfi.e07.Plot;

@Plot(nome = "sin(x) / x", xMin = -20, xMax = 20)
public class Sinc implements Funzione {
    public double f(double x) {
        return Math.sin(x) / x;
    }
}
