package it.unibs.pajc.redolfi.e02;

/** Senza finestra: una pallina lasciata sopra la rampa arriva in fondo e si ferma sul pavimento, mai dentro la rampa. */
public class ProvaRampa {

    public static void main(String[] args) {
        Mondo mondo = RampaApp.scenario();
        Pallina p = mondo.lascia(new Vec2(1.0, 5.0), 0.2, 0.8);
        for (int i = 0; i < 4000; i++) {
            mondo.stepNext(Mondo.PASSO);
            for (Rampa r : mondo.rampe()) {
                double dist = p.posizione().meno(r.puntoPiuVicino(p.posizione())).modulo();
                if (dist < p.raggio() - 0.01)
                    throw new AssertionError("dentro la rampa di " + (p.raggio() - dist) + " m al passo " + i);
            }
        }
        if (!p.ferma() || p.posizione().y() > p.raggio() + 1e-6)
            throw new AssertionError("non si e' fermata sul pavimento: y = " + p.posizione().y() + ", ferma = " + p.ferma());
        System.out.printf("Rampa: ferma sul pavimento in x = %.2f dopo %.1f s%n", p.posizione().x(), mondo.tempo());
    }
}
