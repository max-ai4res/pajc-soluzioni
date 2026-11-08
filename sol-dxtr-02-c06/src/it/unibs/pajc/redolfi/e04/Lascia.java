package it.unibs.pajc.redolfi.e04;

/** Lascia una pallina. Annullare = togliere l'ultima lasciata. */
public record Lascia(Vec2 punto, double raggio, Materiale materiale)
        implements Comando {

    @Override
    public void esegui(Mondo mondo) {
        mondo.lascia(punto, raggio, materiale);
    }

    @Override
    public void annulla(Mondo mondo) {
        mondo.rimuoviUltima();
    }

    @Override
    public boolean annullabile() {
        return true;
    }
}
