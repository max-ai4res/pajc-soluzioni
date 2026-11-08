package it.unibs.pajc.redolfi.e04;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un Object Pool: un numero fisso di particelle create una volta; chi ne vuole una prende
 * la prima non viva e la riaccende. Se sono tutte vive, lo schizzo salta: meglio una
 * scheggia in meno che un oggetto in piu.
 */
public class PoolParticelle {

    private final List<Particella> tutte = new ArrayList<>();

    public PoolParticelle(int quante) {
        for (int i = 0; i < quante; i++)
            tutte.add(new Particella());
    }

    /** Una particella spenta da riaccendere, o null se sono tutte in volo. */
    public Particella prendi(Vec2 posizione, Vec2 velocita) {
        for (Particella p : tutte)
            if (!p.viva()) {
                p.accendi(posizione, velocita);
                return p;
            }
        return null;
    }

    public void spegniTutte() {
        for (Particella p : tutte)
            p.spegni();
    }

    /** Le particelle, vive e spente: la vista disegna solo le vive. */
    public List<Particella> tutte() {
        return Collections.unmodifiableList(tutte);
    }
}
