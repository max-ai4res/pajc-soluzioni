package it.unibs.pajc.redolfi.e09;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Lo stato del tris dal lato del client: la griglia, il mio simbolo, di chi e' il turno,
 * la fase (fuori, in attesa di un avversario, in corso, finita) e l'esito. Non sa di
 * socket ne' di Swing; avvisa con "griglia", "fase", "turno", "esito", "errore".
 */
public class TrisModel {

    public enum Fase { DISCONNESSO, IN_ATTESA, IN_CORSO, FINITA }

    private final PropertyChangeSupport ascoltatori = new PropertyChangeSupport(this);
    private final char[][] griglia = new char[3][3];
    private char mio = ' ';
    private String avversario;
    private boolean turnoMio;
    private Fase fase = Fase.DISCONNESSO;
    private String esito = "";

    public TrisModel() { azzera(); }

    public void addPropertyChangeListener(PropertyChangeListener l) { ascoltatori.addPropertyChangeListener(l); }

    public char cella(int r, int c) { return griglia[r][c]; }
    public char mio() { return mio; }
    public String avversario() { return avversario; }
    public boolean turnoMio() { return turnoMio; }
    public Fase fase() { return fase; }
    public String esito() { return esito; }

    private void azzera() { for (char[] r : griglia) java.util.Arrays.fill(r, ' '); }

    public void setFase(Fase f) { Fase v = fase; fase = f; ascoltatori.firePropertyChange("fase", v, f); }

    public void nuovaPartita(char simbolo, String avversario) {
        azzera();
        mio = simbolo;
        this.avversario = avversario;
        turnoMio = false;
        esito = "";
        ascoltatori.firePropertyChange("griglia", null, this);
        setFase(Fase.IN_CORSO);
    }

    public void setTurnoMio(boolean t) { boolean v = turnoMio; turnoMio = t; ascoltatori.firePropertyChange("turno", v, t); }

    public void mossa(char simbolo, int r, int c) {
        griglia[r][c] = simbolo;
        turnoMio = false;
        ascoltatori.firePropertyChange("griglia", null, this);
    }

    public void fine(String esito) {
        this.esito = esito;
        turnoMio = false;
        setFase(Fase.FINITA);
        ascoltatori.firePropertyChange("esito", null, esito);
    }

    public void errore(String testo) { ascoltatori.firePropertyChange("errore", null, testo); }
}
