package it.unibs.pajc.redolfi.e04;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rilegge palline.log, rifa' la scena a passo fisso e conta gli eventi che coincidono. */
public class Riproduzione {

    record Riga(double t, String[] campi) {
    }

    public static void main(String[] args) throws IOException {
        String file = args.length > 0 ? args[0] : "palline.log";
        List<Riga> righe = new ArrayList<>();
        for (String s : Files.readAllLines(Path.of(file))) {
            String[] c = s.trim().split("\\s+");
            righe.add(new Riga(Double.parseDouble(c[0]), c));
        }
        List<Riga> attesi = righe.stream().filter(r -> !r.campi()[1].equals("LASCIA")).toList();

        Mondo mondo = new Mondo(8, 6);
        List<String> ottenuti = new ArrayList<>();           // "t TIPO" degli eventi del mondo nuovo
        mondo.addMondoListener(new MondoListener() {
            @Override
            public void rimbalzo(Rimbalzo r) {
                ottenuti.add(String.format(Locale.ROOT, "%.4f RIMBALZO", mondo.tempo()));
            }

            @Override
            public void pallinaFerma(Pallina p) {
                ottenuti.add(String.format(Locale.ROOT, "%.4f FERMA", mondo.tempo()));
            }
        });

        double fine = righe.isEmpty() ? 0 : righe.get(righe.size() - 1).t() + 5;
        int prossima = 0;
        while (mondo.tempo() < fine) {
            while (prossima < righe.size() && righe.get(prossima).campi()[1].equals("LASCIA")
                    && righe.get(prossima).t() <= mondo.tempo()) {
                String[] c = righe.get(prossima).campi();
                mondo.lascia(new Vec2(Double.parseDouble(c[2]), Double.parseDouble(c[3])), Double.parseDouble(c[4]));
                prossima++;
            }
            while (prossima < righe.size() && !righe.get(prossima).campi()[1].equals("LASCIA"))
                prossima++;
            mondo.stepNext(0.01);
        }

        int coincidono = 0;
        for (Riga r : attesi)
            for (String o : ottenuti) {
                String[] c = o.split(" ");
                if (c[1].equals(r.campi()[1]) && Math.abs(Double.parseDouble(c[0]) - r.t()) <= 0.001) {
                    coincidono++;
                    break;
                }
            }
        System.out.printf("eventi registrati: %d, riprodotti: %d, coincidenti entro 1 ms: %d%n",
                attesi.size(), ottenuti.size(), coincidono);
    }
}
