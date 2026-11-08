package it.unibs.pajc.redolfi.e01;

import java.util.ArrayList;
import java.util.List;

/**
 * Esercizio 4.1, la legge del rimbalzo.
 *
 * Idea: si lascia cadere una pallina nel Mondo del capitolo e si campiona la quota a
 * ogni passo fisso, tenendo il massimo fra un evento Rimbalzo e il successivo: e' l'altezza
 * raggiunta dopo quel rimbalzo. Si confronta con h_n = h0 * e^(2n).
 *
 * L'errore residuo viene da tre cose: il passo (la pallina tocca terra "in mezzo" a un
 * passo, e la velocita' d'impatto che si inverte e' quella di fine passo, un po' piu'
 * grande di quella vera); la correzione di penetrazione, che la rialza di qualche
 * millimetro regalandole energia potenziale; la quiete, che sotto la soglia ferma la
 * pallina prima della legge.
 *
 * Errore tipico: leggere la quota a un istante fisso dopo il rimbalzo, invece di cercare
 * il massimo fra due rimbalzi.
 */
public class LeggeDelRimbalzo {

    public static void main(String[] args) {
        for (double h0 : new double[] { 2.0, 5.0 })
            for (double e : new double[] { 0.5, 0.8 })
                tabella(h0, e);
    }

    static void tabella(double h0, double e) {
        Mondo mondo = new Mondo(8, 6);
        double raggio = 0.001;                        // trascurabile: la quota e' quella del centro
        Pallina p = mondo.lascia(new Vec2(4, h0 + raggio), raggio, e);
        List<Double> massimi = new ArrayList<>();     // altezza massima dopo ogni rimbalzo
        double[] maxCorrente = { 0 };
        boolean[] rimbalzato = { false };
        mondo.addMondoListener(new MondoListener() {
            @Override
            public void rimbalzo(Rimbalzo r) {
                if (rimbalzato[0])
                    massimi.add(maxCorrente[0]);
                rimbalzato[0] = true;
                maxCorrente[0] = 0;
            }
        });
        for (int i = 0; i < 4000 && massimi.size() < 5; i++) {
            mondo.stepNext(Mondo.PASSO);
            maxCorrente[0] = Math.max(maxCorrente[0], p.posizione().y() - raggio);
        }
        System.out.printf("%nh0 = %.1f m, e = %.1f%n  n   misurata  prevista  errore%n", h0, e);
        for (int n = 1; n <= massimi.size(); n++) {
            double prevista = h0 * Math.pow(e, 2 * n), misurata = massimi.get(n - 1);
            System.out.printf("  %d   %7.4f   %7.4f   %+5.1f %%%n", n, misurata, prevista,
                    100 * (misurata - prevista) / prevista);
        }
    }
}
