package it.unibs.pajc.redolfi.e07.funzioni;

import it.unibs.pajc.redolfi.e07.Plot;

/** Funzioni come metodi statici annotati: una riga ciascuna. */
public class Polinomi {
    @Plot(nome = "x^2", xMin = -3, xMax = 3)
    public static double quadrato(double x) { return x * x; }

    @Plot(nome = "x^3 - 3x", xMin = -3, xMax = 3)
    public static double cubica(double x) { return x * x * x - 3 * x; }

    @Plot(nome = "x sin(x)", xMin = -20, xMax = 20)
    public static double xsinx(double x) { return x * Math.sin(x); }

    public static double aiuto(double x) { return 0; }      // senza @Plot: non nel men\`u
}
