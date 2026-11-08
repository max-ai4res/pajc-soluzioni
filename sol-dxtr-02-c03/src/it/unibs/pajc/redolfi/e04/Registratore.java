package it.unibs.pajc.redolfi.e04;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/** Ascolta il mondo e il pannello e scrive una riga per evento, con il tempo del mondo. */
public class Registratore implements MondoListener, PnlMondo.PuntoListener, AutoCloseable {

    private final Mondo mondo;
    private final PrintWriter out;
    private static final double RAGGIO = 0.25;

    public Registratore(Mondo mondo, PnlMondo vista, String file) throws IOException {
        this.mondo = mondo;
        this.out = new PrintWriter(file);
        mondo.addMondoListener(this);
        vista.addPuntoListener(this);
    }

    private void riga(String formato, Object... args) {
        out.println(String.format(Locale.ROOT, "%.4f ", mondo.tempo()) + String.format(Locale.ROOT, formato, args));
        out.flush();
    }

    @Override
    public void puntoScelto(Vec2 p) {
        riga("LASCIA %.4f %.4f %.4f", p.x(), p.y(), RAGGIO);
    }

    @Override
    public void rimbalzo(Rimbalzo r) {
        riga("RIMBALZO %.4f %.4f", r.pallina().posizione().x(), r.velocita());
    }

    @Override
    public void pallinaFerma(Pallina p) {
        riga("FERMA %.4f", p.posizione().x());
    }

    @Override
    public void close() {
        mondo.removeMondoListener(this);
        out.close();
    }
}
