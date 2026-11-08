package it.unibs.pajc.redolfi.e04;

public enum Direzione {
    SU(0, -1), GIU(0, 1), SINISTRA(-1, 0), DESTRA(1, 0);

    public final int dc, dr;

    Direzione(int dc, int dr) {
        this.dc = dc;
        this.dr = dr;
    }

    public boolean oppostaA(Direzione d) {
        return dc == -d.dc && dr == -d.dr;
    }
}
