package it.unibs.pajc.redolfi.e07;

import java.util.ArrayList;
import java.util.List;

/** Una classe di test che esercita tutte le annotazioni. */
public class ProvaStringhe {

    private List<String> parole;
    static int aperture, chiusure;

    @Prima public void prepara() { parole = new ArrayList<>(List.of("pera", "mela", "kiwi")); aperture++; }
    @Dopo public void chiudi() { chiusure++; }

    @Test public void ordinamento() { parole.sort(null); verifica(parole.get(0).equals("kiwi")); }
    @Test public void lunghezza() { verifica("reflection".length() == 10); }
    @Test(atteso = IndexOutOfBoundsException.class) public void fuoriDaiLimiti() { parole.get(10); }
    @Test(atteso = IllegalStateException.class) public void eccezioneSbagliata() { parole.get(10); }
    @Test(timeout = 100) public void veloce() { verifica("a".repeat(1000).length() == 1000); }
    @Test(timeout = 100) public void troppoLento() throws InterruptedException { Thread.sleep(2000); }
    @Test public void questoFallisce() { verifica(parole.contains("uva")); }

    static void verifica(boolean c) { if (!c) throw new AssertionError("condizione falsa"); }
}
