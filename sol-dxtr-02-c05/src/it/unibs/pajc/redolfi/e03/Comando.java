package it.unibs.pajc.redolfi.e03;

import java.util.List;

/** Un comando sull'elenco delle figure: si esegue e si annulla. */
public interface Comando {

    void esegui(List<Figura> figure);

    void annulla(List<Figura> figure);
}
