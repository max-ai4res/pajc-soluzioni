package it.unibs.pajc.redolfi.e03;

/** L'aliante dopo quattro generazioni e' se' stesso spostato di una cella in diagonale. */
public class ProvaGriglia {

    public static void main(String[] args) {
        Griglia g = new Griglia(10, 10);
        int[][] aliante = { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 2, 1 }, { 2, 2 } };
        for (int[] c : aliante)
            g.accendi(c[0], c[1]);
        for (int i = 0; i < 4; i++)
            g.generazione();
        int vive = 0;
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++)
                if (g.viva(r, c))
                    vive++;
        boolean ok = vive == 5;
        for (int[] c : aliante)
            ok &= g.viva(c[0] + 1, c[1] + 1);
        if (!ok)
            throw new AssertionError("l'aliante non si e' spostato come doveva");
        System.out.println("Griglia: l'aliante vola (anche attraverso il bordo del toro)");

        Griglia bordo = new Griglia(6, 6);              // lo stesso, a cavallo del bordo
        for (int[] c : aliante)
            bordo.accendi(c[0] + 4, c[1] + 4);
        for (int i = 0; i < 24; i++)
            bordo.generazione();
        for (int[] c : aliante)
            if (!bordo.viva(c[0] + 4 + 6, c[1] + 4 + 6))
                throw new AssertionError("l'aliante si e' rotto sul bordo");
        System.out.println("Griglia: dopo 24 generazioni su un toro 6x6 e' tornato al via");
    }
}
