package it.unibs.pajc.redolfi.plugin;

import java.util.List;

import it.unibs.pajc.redolfi.e08.Comando;

public class Inverti implements Comando {
    public String nome() { return "inverti"; }
    public String aiuto() { return "stampa gli argomenti al contrario"; }
    public void esegui(List<String> argomenti) {
        for (String a : argomenti)
            System.out.print(new StringBuilder(a).reverse() + " ");
        System.out.println();
    }
}
