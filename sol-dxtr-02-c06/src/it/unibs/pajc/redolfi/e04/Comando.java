package it.unibs.pajc.redolfi.e04;

/** Un'azione sul mondo, come oggetto: si esegue, si puo annullare, si puo registrare. */
public interface Comando {

    void esegui(Mondo mondo);

    /** Disfa cio' che esegui ha fatto. Di norma non si puo: non fa niente. */
    default void annulla(Mondo mondo) {
    }

    default boolean annullabile() {
        return false;
    }
}
