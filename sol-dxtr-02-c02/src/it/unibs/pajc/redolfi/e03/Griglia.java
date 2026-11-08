package it.unibs.pajc.redolfi.e03;

/** Il modello: celle vive o morte su un toro, le regole di Conway. Niente Swing. */
public class Griglia {

    private final int righe, colonne;
    private boolean[][] celle;

    public Griglia(int righe, int colonne) {
        this.righe = righe;
        this.colonne = colonne;
        this.celle = new boolean[righe][colonne];
    }

    public int righe() {
        return righe;
    }

    public int colonne() {
        return colonne;
    }

    public boolean viva(int r, int c) {
        return celle[Math.floorMod(r, righe)][Math.floorMod(c, colonne)];
    }

    public void inverti(int r, int c) {
        celle[r][c] = !celle[r][c];
    }

    public void accendi(int r, int c) {
        celle[Math.floorMod(r, righe)][Math.floorMod(c, colonne)] = true;
    }

    public void svuota() {
        celle = new boolean[righe][colonne];
    }

    private int vicineVive(int r, int c) {
        int n = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++)
                if ((dr != 0 || dc != 0) && viva(r + dr, c + dc))
                    n++;
        return n;
    }

    /** Una generazione: si calcola in una griglia nuova e si scambia alla fine. */
    public void generazione() {
        boolean[][] nuove = new boolean[righe][colonne];
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++) {
                int n = vicineVive(r, c);
                nuove[r][c] = celle[r][c] ? (n == 2 || n == 3) : n == 3;
            }
        celle = nuove;
    }
}
