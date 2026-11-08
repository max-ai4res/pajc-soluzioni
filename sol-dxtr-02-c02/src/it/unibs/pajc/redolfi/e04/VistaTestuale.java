package it.unibs.pajc.redolfi.e04;

import java.util.Comparator;

/** Una vista che non e' un componente Swing: una riga a console con tre numeri letti dal mondo. */
public class VistaTestuale {

    public void aggiorna(Mondo mondo) {
        var palline = mondo.palline();
        if (palline.isEmpty()) {
            System.out.println("nessuna pallina");
            return;
        }
        double piuAlta = palline.stream().mapToDouble(p -> p.posizione().y()).max().orElse(0);
        Pallina piuVeloce = palline.stream()
                .max(Comparator.comparingDouble(p -> p.velocita().modulo())).orElseThrow();
        System.out.printf("%d palline   la piu' alta a %.2f m   la piu' veloce a %.2f m/s%n",
                palline.size(), piuAlta, piuVeloce.velocita().modulo());
    }
}
