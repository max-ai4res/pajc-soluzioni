package it.unibs.pajc.redolfi.e07;

/** Senza finestra: l'invulnerabilita' dura tre secondi e poi finisce; il toro riporta dentro chi esce. */
public class ProvaUniverso {

    public static void main(String[] args) {
        Universo u = new Universo(7);
        int vitePrima = u.vite();
        Navicella n = u.navicella();
        n.rinasci(n.posizione());                        // appena rinata: invulnerabile
        if (!n.invulnerabile())
            throw new AssertionError("appena rinata deve essere invulnerabile");
        for (int i = 0; i < 200; i++)
            n.stepNext(0.01, Universo.LARGHEZZA, Universo.ALTEZZA);   // 2 s
        if (!n.invulnerabile())
            throw new AssertionError("a 2 s deve essere ancora invulnerabile");
        for (int i = 0; i < 200; i++)
            n.stepNext(0.01, Universo.LARGHEZZA, Universo.ALTEZZA);   // 4 s
        if (n.invulnerabile())
            throw new AssertionError("a 4 s l'invulnerabilita' deve essere finita");
        System.out.println("invulnerabilita': 0 s si, 2 s si, 4 s no: ok (vite " + vitePrima + ")");
        Asteroide a = new Asteroide(new Vec2(23.9, 8), new Vec2(2, 0), Asteroide.Taglia.PICCOLO, 0);
        a.stepNext(0.1, Universo.LARGHEZZA, Universo.ALTEZZA);
        if (a.posizione().x() > 1)
            throw new AssertionError("il toro non ha riportato dentro l'asteroide: x = " + a.posizione().x());
        System.out.printf("toro: da x = 23,9 a x = %.2f, ok%n", a.posizione().x());
    }
}
