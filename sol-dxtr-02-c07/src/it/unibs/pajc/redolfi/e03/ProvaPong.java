package it.unibs.pajc.redolfi.e03;

/** Senza finestra: la pallina verso la racchetta ferma torna indietro; verso il lato scoperto fa punto. */
public class ProvaPong {

    public static void main(String[] args) {
        Pong p = new Pong();
        p.lancia(new Vec2(-8, 0));                        // verso la racchetta A, ferma al centro
        for (int i = 0; i < 100; i++)
            p.stepNext(0.01);
        if (p.velocita().x() <= 0 || p.puntiB() != 0)
            throw new AssertionError("doveva rimbalzare sulla racchetta A: v = " + p.velocita());
        System.out.println("rimbalzo sulla racchetta: ok, v = " + p.velocita());
        Pong q = new Pong();
        q.muovi(1, 0);                                    // la racchetta A sale fino in cima: il lato resta scoperto
        for (int i = 0; i < 100; i++)
            q.stepNext(0.01);
        q.lancia(new Vec2(-8, 0));
        for (int i = 0; i < 300; i++)
            q.stepNext(0.01);
        if (q.puntiB() != 1)
            throw new AssertionError("doveva essere punto per B: " + q.puntiB());
        System.out.println("punto sul lato scoperto: ok, " + q.puntiA() + " : " + q.puntiB());
    }
}
