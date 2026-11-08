package it.unibs.pajc.redolfi.e03;

/** Senza finestra: 100 spari in un secondo, mai piu di 64 in volo, dopo tre secondi tutti liberi. */
public class ProvaProiettili {

    public static void main(String[] args) {
        Navicella n = new Navicella();
        int partiti = 0;
        for (int i = 0; i < 100; i++) {
            if (n.spara())
                partiti++;
            n.ruota(1);
            n.stepNext(0.01);
            if (n.pool().inVolo() > 64)
                throw new AssertionError("piu di 64 in volo");
        }
        System.out.println("partiti " + partiti + " su 100 spari, in volo " + n.pool().inVolo());
        for (int i = 0; i < 300; i++)
            n.stepNext(0.01);
        if (n.pool().liberi() != 64)
            throw new AssertionError("dopo tre secondi dovrebbero essere tutti liberi: " + n.pool().liberi());
        System.out.println("dopo tre secondi: liberi " + n.pool().liberi());
    }
}
