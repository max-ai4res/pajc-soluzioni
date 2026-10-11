package it.unibs.pajc.redolfi.e08;

/** Un conto con identificatore e saldo; i metodi non sono sincronizzati: lo fa trasferisci. */
public class Conto {
    private final int id;
    private int saldo;

    public Conto(int id, int saldo) { this.id = id; this.saldo = saldo; }

    public int id() { return id; }
    public int saldo() { return saldo; }
    public void preleva(int x) { saldo -= x; }
    public void versa(int x) { saldo += x; }
}
