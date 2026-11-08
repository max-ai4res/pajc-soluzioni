package it.unibs.pajc.redolfi.e08;

/** La fabbrica legge cio' che scrive: un mondo salvato e ricaricato ha le stesse palline e gli stessi ostacoli. */
public class ProvaScenario {

    public static void main(String[] args) {
        Mondo m = FabbricaScenari.daRisorsa("piramide");
        for (int i = 0; i < 100; i++)
            m.stepNext(0.01);
        String testo = FabbricaScenari.aTesto(m);
        Mondo r = FabbricaScenari.daTesto(testo);
        if (r.palline().size() != m.palline().size() || r.ostacoli().size() != m.ostacoli().size())
            throw new AssertionError("ricaricato diverso: " + r.palline().size() + " palline");
        for (int i = 0; i < m.palline().size(); i++)
            if (m.palline().get(i).posizione().meno(r.palline().get(i).posizione()).modulo() > 0.001
                    || m.palline().get(i).materiale() != r.palline().get(i).materiale())
                throw new AssertionError("pallina " + i + " diversa");
        System.out.println("scenario salvato e riletto: " + r.palline().size() + " palline, " + r.ostacoli().size() + " ostacoli, ok");
        System.out.print(testo.lines().limit(4).reduce("", (a, b) -> a + b + "\n"));
    }
}
