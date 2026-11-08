package it.unibs.pajc.redolfi.e01;

/** Un minuto di termostato senza finestra: la console degli accesi e spenti. */
public class ProvaTermostato {

    public static void main(String[] args) {
        Termostato t = new Termostato();
        t.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("riscaldamentoAcceso"))
                System.out.printf("t = %5.1f s  misurata %.2f  riscaldamento %s%n", t.tempo(), t.misurata(),
                        (Boolean) e.getNewValue() ? "ACCESO" : "spento");
        });
        System.out.println("all'avvio: " + (t.riscaldamentoAcceso() ? "acceso" : "spento"));
        for (int decimo = 0; decimo < 600; decimo++) {
            if (decimo == 300)
                t.setDesiderata(16);                 // a 30 s si abbassa l'obiettivo
            t.stepNext(0.1);
        }
    }
}
