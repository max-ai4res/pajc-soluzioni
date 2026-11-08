package it.unibs.pajc.redolfi.e08;

import java.util.List;

/** Il contratto fra l'applicazione e i suoi plugin: sta qui, e solo qui. */
public interface Comando {
    String nome();
    String aiuto();
    void esegui(List<String> argomenti);
}
