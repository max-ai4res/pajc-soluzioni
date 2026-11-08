package it.unibs.pajc.redolfi.e02;

/** Una sequenza di comandi senza finestra, con le verifiche su stato e credito. */
public class ProvaDistributore {

    static void verifica(boolean c, String m) {
        if (!c)
            throw new AssertionError(m);
    }

    public static void main(String[] args) {
        Distributore d = new Distributore();
        d.addDistributoreListener(new DistributoreListener() {
            @Override public void messaggio(String t)        { System.out.println("  [display] " + t); }
            @Override public void prodottoErogato(String p)  { System.out.println("  [cade] " + p); }
            @Override public void restoDato(int c)           { System.out.println("  [resto] " + c + " cent"); }
            @Override public void statoCambiato(String n)    { System.out.println("  -> " + n); }
        });
        d.esegui(new Scelta("caffe"));
        verifica(d.stato() instanceof InAttesa, "senza credito resta in attesa");
        d.esegui(new Moneta(50));
        verifica(d.stato() instanceof ConCredito && d.credito() == 50, "con credito, 50");
        d.esegui(new Scelta("caffe"));
        verifica(d.stato() instanceof ConCredito, "mancano 50: resta con credito");
        d.esegui(new Moneta(100));
        d.esegui(new Scelta("caffe"));
        verifica(d.stato() instanceof InErogazione && d.credito() == 50, "in erogazione, resta 50");
        d.esegui(new Moneta(20));                        // restituita
        for (int i = 0; i < 35; i++)
            d.stepNext(0.1);
        verifica(d.stato() instanceof ConCredito && d.credito() == 50, "erogato, torna con credito 50");
        d.esegui(new Resto());
        verifica(d.stato() instanceof InAttesa && d.credito() == 0, "resto dato, in attesa");
        d.esegui(new Guasto());
        d.esegui(new Moneta(10));
        verifica(d.stato() instanceof FuoriServizio, "fuori servizio, moneta restituita");
        d.esegui(new Riparato());
        verifica(d.stato() instanceof InAttesa, "riparato");
        System.out.println("Distributore: tutto ok");
    }
}
