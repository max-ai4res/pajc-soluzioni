package it.unibs.pajc.redolfi.e03;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoolProiettili {

    private final List<Proiettile> tutti = new ArrayList<>();

    public PoolProiettili(int quanti) {
        for (int i = 0; i < quanti; i++)
            tutti.add(new Proiettile());
    }

    /** Spara se c'e' un proiettile libero: vero se e' partito. */
    public boolean spara(Vec2 da, Vec2 v) {
        for (Proiettile p : tutti)
            if (!p.vivo()) {
                p.spara(da, v);
                return true;
            }
        return false;
    }

    public long inVolo() {
        return tutti.stream().filter(Proiettile::vivo).count();
    }

    public int liberi() {
        return tutti.size() - (int) inVolo();
    }

    public List<Proiettile> tutti() {
        return Collections.unmodifiableList(tutti);
    }
}
