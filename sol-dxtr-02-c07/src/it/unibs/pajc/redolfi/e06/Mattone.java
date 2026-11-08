package it.unibs.pajc.redolfi.e06;

/** Un mattone: un ostacolo con un materiale e i colpi che gli restano. */
public class Mattone {

    public enum Tipo {
        VETRO(1, 10), PIETRA(3, 30), ACCIAIO(Integer.MAX_VALUE, 0);

        public final int colpi, punti;

        Tipo(int colpi, int punti) {
            this.colpi = colpi;
            this.punti = punti;
        }
    }

    private final Ostacolo forma;
    private final Tipo tipo;
    private int colpiResidui;

    public Mattone(Ostacolo forma, Tipo tipo) {
        this.forma = forma;
        this.tipo = tipo;
        this.colpiResidui = tipo.colpi;
    }

    public Ostacolo forma()  { return forma; }
    public Tipo tipo()       { return tipo; }
    public int colpiResidui() { return colpiResidui; }
    public boolean rotto()   { return colpiResidui <= 0; }

    void colpito() {
        if (tipo != Tipo.ACCIAIO)
            colpiResidui--;
    }
}
