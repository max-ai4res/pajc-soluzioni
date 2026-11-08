package it.unibs.pajc.redolfi.e04;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/** Il modello: toro, regole di Conway, l'evento generazione, la velocita' come proprieta'. */
public class Griglia {

    public interface Listener {
        void generazione(int numero, int vive);
    }

    private final int righe, colonne;
    private boolean[][] celle;
    private int numero;
    private double velocita = 5;                          // generazioni al secondo
    private final List<Listener> listeners = new ArrayList<>();
    private final PropertyChangeSupport proprieta = new PropertyChangeSupport(this);

    public Griglia(int righe, int colonne) {
        this.righe = righe;
        this.colonne = colonne;
        this.celle = new boolean[righe][colonne];
    }

    public void addListener(Listener l) { listeners.add(l); }
    public void addPropertyChangeListener(PropertyChangeListener l) { proprieta.addPropertyChangeListener(l); }

    public int righe()      { return righe; }
    public int colonne()    { return colonne; }
    public int numero()     { return numero; }
    public double velocita() { return velocita; }

    public void setVelocita(double v) {
        double vecchia = velocita;
        velocita = v;
        proprieta.firePropertyChange("velocita", vecchia, v);
    }

    public boolean viva(int r, int c) {
        return celle[Math.floorMod(r, righe)][Math.floorMod(c, colonne)];
    }

    public void inverti(int r, int c) {
        celle[r][c] = !celle[r][c];
        annuncia();
    }

    /** Posa un pattern (righe di . e O) con l'angolo in alto a sinistra in (r, c). */
    public void posa(List<String> pattern, int r, int c) {
        for (int i = 0; i < pattern.size(); i++)
            for (int j = 0; j < pattern.get(i).length(); j++)
                if (pattern.get(i).charAt(j) == 'O')
                    celle[Math.floorMod(r + i, righe)][Math.floorMod(c + j, colonne)] = true;
        annuncia();
    }

    public void svuota() {
        celle = new boolean[righe][colonne];
        numero = 0;
        annuncia();
    }

    public int vive() {
        int n = 0;
        for (boolean[] riga : celle)
            for (boolean v : riga)
                if (v)
                    n++;
        return n;
    }

    private int vicine(int r, int c) {
        int n = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++)
                if ((dr != 0 || dc != 0) && viva(r + dr, c + dc))
                    n++;
        return n;
    }

    public void generazione() {
        boolean[][] nuove = new boolean[righe][colonne];
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++) {
                int n = vicine(r, c);
                nuove[r][c] = celle[r][c] ? (n == 2 || n == 3) : n == 3;
            }
        celle = nuove;
        numero++;
        annuncia();
    }

    private void annuncia() {
        int vive = vive();
        for (Listener l : List.copyOf(listeners))
            l.generazione(numero, vive);
    }
}
