package it.unibs.pajc.redolfi.e08;

import java.util.List;

/** Svuota il mondo. Per annullare serve un'istantanea di prima: un Memento. */
public class Svuota implements Comando {

    private List<Pallina> prima;

    @Override
    public void esegui(Mondo mondo) {
        prima = List.copyOf(mondo.palline());
        mondo.svuota();
    }

    @Override
    public void annulla(Mondo mondo) {
        mondo.ripristina(prima);
    }

    @Override
    public boolean annullabile() {
        return true;
    }
}
