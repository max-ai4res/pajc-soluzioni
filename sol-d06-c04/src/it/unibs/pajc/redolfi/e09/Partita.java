package it.unibs.pajc.redolfi.e09;

/**
 * Una partita di tris fra due sessioni: la griglia, il turno, l'esito. E' l'oggetto
 * condiviso fra i due thread delle sessioni: ogni metodo che lo legge o lo modifica e'
 * synchronized, cosi' due mosse "contemporanee" vengono messe in fila e la seconda
 * trova il turno gia' passato. Gli avvisi ai due giocatori partono da qui, con il lock
 * preso: dura pochi microsecondi (println in un buffer), accettabile anche sui thread
 * virtuali di Java 21.
 */
public class Partita {
    private final Sessione x, o;
    private final char[][] griglia = new char[3][3];
    private char turno = 'X';
    private boolean finita;

    Partita(Sessione x, Sessione o) {
        this.x = x;
        this.o = o;
        for (char[] r : griglia) java.util.Arrays.fill(r, '.');
    }

    synchronized void inizia() {
        x.invia("PARTITA X " + o.nome());
        o.invia("PARTITA O " + x.nome());
        x.invia("TOCCA");
    }

    char simboloDi(Sessione s) { return s == x ? 'X' : 'O'; }

    /** Applica la mossa di chi la chiede; risponde ERR a chi sbaglia, MOSSA a entrambi. */
    synchronized void mossa(Sessione chi, int r, int k) {
        char s = simboloDi(chi);
        if (finita) { chi.invia("ERR la partita e' finita"); return; }
        if (s != turno) { chi.invia("ERR non e' il tuo turno"); return; }
        if (griglia[r][k] != '.') { chi.invia("ERR casella occupata"); return; }
        griglia[r][k] = s;
        aEntrambi("MOSSA " + s + " " + r + " " + k);
        if (vince(s)) { finita = true; aEntrambi("VINCE " + s); return; }
        if (piena()) { finita = true; aEntrambi("PATTA"); return; }
        turno = s == 'X' ? 'O' : 'X';
        (turno == 'X' ? x : o).invia("TOCCA");
    }

    /** Un giocatore se ne va: l'altro vince a tavolino. */
    synchronized void abbandona(Sessione chi) {
        if (finita) return;
        finita = true;
        (chi == x ? o : x).invia("ABBANDONO " + chi.nome());
    }

    synchronized boolean finita() { return finita; }

    Sessione avversarioDi(Sessione s) { return s == x ? o : x; }

    private void aEntrambi(String riga) { x.invia(riga); o.invia(riga); }

    private boolean vince(char s) {
        for (int i = 0; i < 3; i++) {
            if (griglia[i][0] == s && griglia[i][1] == s && griglia[i][2] == s) return true;
            if (griglia[0][i] == s && griglia[1][i] == s && griglia[2][i] == s) return true;
        }
        return (griglia[0][0] == s && griglia[1][1] == s && griglia[2][2] == s)
            || (griglia[0][2] == s && griglia[1][1] == s && griglia[2][0] == s);
    }

    private boolean piena() {
        for (char[] r : griglia) for (char c : r) if (c == '.') return false;
        return true;
    }
}
