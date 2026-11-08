package it.unibs.pajc.redolfi.e04;

/** Senza finestra: con un seme fisso si guida il serpente sulla mela, poi contro il muro. */
public class ProvaSerpente {

    public static void main(String[] args) {
        Serpente s = new Serpente(10, 10, 42);
        Cella mela = s.mela();
        System.out.println("testa " + s.testa() + ", mela " + mela);
        int passi = 0;
        while (!s.testa().equals(mela) && passi < 100 && !s.finita()) {
            Cella t = s.testa();
            if (t.riga() != mela.riga())
                s.gira(t.riga() > mela.riga() ? Direzione.SU : Direzione.GIU);
            else
                s.gira(t.colonna() > mela.colonna() ? Direzione.SINISTRA : Direzione.DESTRA);
            s.stepNext();
            passi++;
        }
        if (s.punti() != 10 || s.finita())
            throw new AssertionError("non ha mangiato la mela: punti " + s.punti() + ", finita " + s.finita());
        System.out.println("mela mangiata in " + passi + " passi, punti " + s.punti());
        Serpente u = new Serpente(10, 10, 1);            // due frecce in un intervallo: in coda, non a 180 gradi
        u.gira(Direzione.SU);
        u.gira(Direzione.SINISTRA);
        u.stepNext();
        u.stepNext();
        if (u.finita())
            throw new AssertionError("si e' morso: la direzione e' stata applicata subito");
        for (int i = 0; i < 20 && !u.finita(); i++)
            u.stepNext();
        if (!u.finita())
            throw new AssertionError("avrebbe dovuto urtare il muro");
        System.out.println("partita finita contro il muro, come previsto");
    }
}
