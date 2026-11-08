package it.unibs.pajc.redolfi.e06;

/** Senza finestra: la pallina lanciata rompe mattoni e non ne attraversa nessuno; il punteggio sale. */
public class ProvaMattoni {

    public static void main(String[] args) {
        Mattoni m = new Mattoni();
        m.caricaLivello(FabbricaLivelli.daRisorsa(1));
        int prima = m.mattoni().size();
        int[] rotti = { 0 };
        m.addListener(new Mattoni.Listener() {
            @Override public void mattoneRotto(Mattone mt, int p) { rotti[0]++; }
        });
        m.lancia();
        for (int i = 0; i < 1500; i++) {
            m.stepNext(0.01);
            for (Mattone mt : m.mattoni()) {             // la pallina non e' mai dentro un mattone
                Ostacolo o = mt.forma();
                Vec2 p = m.pallina();
                if (p.x() > o.x() + 0.01 && p.x() < o.x() + o.larghezza() - 0.01 && p.y() > o.y() + 0.01 && p.y() < o.y() + o.altezza() - 0.01)
                    throw new AssertionError("pallina dentro un mattone al passo " + i);
            }
        }
        System.out.printf("mattoni: %d -> %d, rotti %d, punti %d, vite %d%n", prima, m.mattoni().size(), rotti[0], m.punteggio(), m.vite());
        if (rotti[0] == 0)
            throw new AssertionError("nessun mattone rotto");
    }
}
