package it.unibs.pajc.redolfi.plugin;

import java.util.List;

import it.unibs.pajc.redolfi.e08.Comando;

public class Conta implements Comando {
    public String nome() { return "conta"; }
    public String aiuto() { return "conta le parole e i caratteri degli argomenti"; }
    public void esegui(List<String> argomenti) {
        int caratteri = argomenti.stream().mapToInt(String::length).sum();
        System.out.println(argomenti.size() + " parole, " + caratteri + " caratteri");
    }
}
