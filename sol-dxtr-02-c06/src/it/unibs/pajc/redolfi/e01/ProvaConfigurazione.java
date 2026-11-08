package it.unibs.pajc.redolfi.e01;

/**
 * Esercizio 6.1, la configurazione in due modi.
 *
 * Idea: lo stesso file di proprieta' fra le risorse letto da un enum singleton e da un
 * record costruito nel main. Il mondo riceve la gravita' in entrambi i casi. La
 * differenza si vede nel test: con il record si costruisce una Configurazione con la
 * gravita' lunare e la si passa; con l'enum l'unica gravita' possibile e' quella del
 * file, e per provare la Luna bisognerebbe cambiare il file (e tutti gli altri test).
 *
 * Errore tipico: leggere config.properties con un FileInputStream dalla cartella di
 * lavoro: funziona solo lanciando da Eclipse nella cartella del progetto.
 */
public class ProvaConfigurazione {

    /** Il programma che usa la configurazione: riceve il record, non lo cerca. */
    static Mondo mondoDa(Configurazione c) {
        Mondo m = new Mondo(c.larghezza(), 6);
        m.lascia(new Vec2(c.larghezza() / 2, 5), 0.25);
        for (int i = 0; i < 100; i++)
            m.stepNext(0.01, c.gravita());
        return m;
    }

    public static void main(String[] args) {
        // 1. con il singleton: la gravita' e' quella del file, e basta
        ConfigurazioneGlobale g = ConfigurazioneGlobale.ISTANZA;
        Mondo m1 = new Mondo(g.larghezza(), 6);
        m1.lascia(new Vec2(4, 5), 0.25);
        for (int i = 0; i < 100; i++)
            m1.stepNext(0.01, g.gravita());
        System.out.printf("singleton: g = %.2f, dopo 1 s y = %.2f%n", g.gravita(), m1.palline().get(0).posizione().y());

        // 2. con il record passato: dal file nel main...
        Configurazione c = Configurazione.daRisorsa();
        System.out.printf("record:    g = %.2f, dopo 1 s y = %.2f%n", c.gravita(), mondoDa(c).palline().get(0).posizione().y());
        // ...e con un'altra gravita' in un test, senza toccare il file
        Configurazione luna = new Configurazione(c.larghezza(), 1.62, 0);
        System.out.printf("test Luna: g = %.2f, dopo 1 s y = %.2f%n", luna.gravita(), mondoDa(luna).palline().get(0).posizione().y());
        // con ConfigurazioneGlobale questo terzo caso non si puo' scrivere: ISTANZA.gravita() e' sempre 9,81
    }
}
