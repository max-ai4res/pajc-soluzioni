package it.unibs.pajc.redolfi.e04;

public record Cella(int riga, int colonna) {

    Cella piu(Direzione d) {
        return new Cella(riga + d.dr, colonna + d.dc);
    }
}
