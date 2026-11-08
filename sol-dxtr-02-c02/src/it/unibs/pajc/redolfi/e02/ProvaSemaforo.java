package it.unibs.pajc.redolfi.e02;

/** Il modello provato senza finestra: 60 secondi a passi di un decimo, con una chiamata. */
public class ProvaSemaforo {

    public static void main(String[] args) {
        Semaforo s = new Semaforo();
        Semaforo.Fase ultima = null;
        for (int decimo = 0; decimo <= 600; decimo++) {
            if (decimo == 80)                              // a 8 s: il verde finisce a 10 s
                s.chiamata();
            if (s.fase() != ultima) {
                System.out.printf("t = %4.1f s  %-10s auto %-6s pedoni %s%n", decimo / 10.0,
                        s.fase(), s.luceAuto(), s.lucePedoni());
                ultima = s.fase();
            }
            s.stepNext(0.1);
        }
    }
}
