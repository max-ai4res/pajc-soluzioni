package it.unibs.pajc.redolfi.plugin;

import java.time.LocalDateTime;
import java.util.List;

import it.unibs.pajc.redolfi.e08.Comando;

public class Data implements Comando {
    public String nome() { return "data"; }
    public String aiuto() { return "stampa data e ora"; }
    public void esegui(List<String> argomenti) { System.out.println(LocalDateTime.now()); }
}
